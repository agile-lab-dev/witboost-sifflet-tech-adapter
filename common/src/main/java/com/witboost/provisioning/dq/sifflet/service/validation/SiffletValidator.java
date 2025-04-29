package com.witboost.provisioning.dq.sifflet.service.validation;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.fasterxml.jackson.databind.JsonNode;
import com.witboost.provisioning.dq.sifflet.model.SiffletDataContract;
import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.dq.sifflet.model.athena.AthenaEntity;
import com.witboost.provisioning.dq.sifflet.model.athena.AthenaInfo;
import com.witboost.provisioning.dq.sifflet.utils.ErrorUtils;
import com.witboost.provisioning.model.OutputPort;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.parser.Parser;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

public class SiffletValidator {

    private static final Logger logger = LoggerFactory.getLogger(SiffletValidator.class);

    public static Either<FailedOperation, Void> validateSiffletComponent(@Valid SiffletSpecific siffletSpecific) {
        Validator validator;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        var violations = validator.validate(siffletSpecific);

        if (!violations.isEmpty()) {
            List<Problem> problems = violations.stream()
                    .map(violation -> new Problem(violation.getMessage()))
                    .collect(Collectors.toList());

            logger.error("Validation errors occurred while validating Sifflet specific section: {}", problems);
            return Either.left(new FailedOperation(
                    "Validation error(s) occurred while validating Sifflet specific section", problems));
        }

        return Either.right(null);
    }

    public static Either<FailedOperation, Tuple2<AthenaEntity, SiffletDataContract>>
            extractAndValidateSiffletOutputPortDependency(JsonNode outputPortDependency) {

        var eitherOutputPort = Parser.parseComponent(outputPortDependency, OutputPort.class, Specific.class);
        if (eitherOutputPort.isLeft()) return left(eitherOutputPort.getLeft());

        logger.info("Input JsonNode: {}", outputPortDependency.toPrettyString());
        logger.info("Resolved dependency: {}", outputPortDependency.get("dependency"));

        OutputPort<Specific> outputPort = (OutputPort<Specific>) eitherOutputPort.get();

        List<FailedOperation> errors = new ArrayList<>();

        AthenaEntity athenaEntity = null;
        SiffletDataContract siffletDataContract = null;

        if (outputPort.getInfo().isEmpty()) {
            logger.error("Dependency {} doesn't contain any deployment information", outputPort.getId());
            errors.add(new FailedOperation(
                    String.format("Dependency %s doesn't contain any deployment information", outputPort.getId()),
                    Collections.singletonList(new Problem(String.format(
                            "Dependency %s component on the input descriptor doesn't contain any deployment information. Did you set up correctly the component dependencies?",
                            outputPort.getId())))));
        } else {
            var maybeAthenaInfo = Parser.parseObject(outputPort.getInfo().get(), AthenaInfo.class);
            if (maybeAthenaInfo.isLeft()) {
                logger.error("Parsing of Athena provision info failed: {}", maybeAthenaInfo.getLeft());
                errors.add(maybeAthenaInfo.getLeft());
            } else {
                var athenaInfo = maybeAthenaInfo.get();
                // Extract athena properties from dependency
                var maybeAthenaEntity = getAthenaEntityFromInfo(athenaInfo);
                if (maybeAthenaEntity.isLeft()) return left(maybeAthenaEntity.getLeft());
                athenaEntity = maybeAthenaEntity.get();
            }
        }

        var maybeDataContract = Parser.parseObject(outputPortDependency.get("dataContract"), SiffletDataContract.class);
        if (maybeDataContract.isLeft()) {
            logger.error(
                    "Parsing of Athena data contract with Sifflet monitors failed: {}", maybeDataContract.getLeft());
            errors.add(maybeDataContract.getLeft());
        } else {
            siffletDataContract = maybeDataContract.get();
        }

        if (errors.isEmpty()) {
            return right(new Tuple2<>(athenaEntity, siffletDataContract));
        } else {
            return left(ErrorUtils.mergeSequence(
                            errors.stream().map(Either::left).toList(),
                            "occurred while extracting information from Output Port dependency")
                    .getLeft());
        }
    }

    protected static Either<FailedOperation, AthenaEntity> getAthenaEntityFromInfo(AthenaInfo athenaInfo) {
        Map<String, Optional<String>> neededValues = Stream.of("catalog", "database", "region", "view", "s3Location")
                .map(key -> new Tuple2<>(key, athenaInfo.getInfoValue(key)))
                .collect(Collectors.toMap(t -> t._1, t -> t._2));

        if (neededValues.values().stream().noneMatch(Optional::isEmpty)) {
            return right(new AthenaEntity(
                    neededValues.get("catalog").get(),
                    neededValues.get("database").get(),
                    neededValues.get("view").get(),
                    Region.of(neededValues.get("region").get()),
                    "primary", // Hardcoded as default value
                    neededValues.get("s3Location").get()));
        } else {
            var problems = neededValues.entrySet().stream()
                    .filter(entry -> entry.getValue().isEmpty())
                    .map(entry -> new Problem(String.format(
                            "Athena dependency doesn't contain field '%s' on private deployment info", entry.getKey())))
                    .toList();
            logger.error("Problems encountered while retrieving Athena information: {}", problems);
            return left(new FailedOperation(
                    String.format("%s problems encountered while retrieving Athena information", problems.size()),
                    problems));
        }
    }
}
