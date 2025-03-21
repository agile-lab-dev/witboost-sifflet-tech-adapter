package com.witboost.provisioning.dq.sifflet.utils;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.witboost.provisioning.model.common.FailedOperation;
import io.vavr.control.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ErrorUtils {

    public static <T> Either<FailedOperation, List<T>> mergeSequence(
            List<Either<FailedOperation, T>> errors, String messageTemplate) {
        return errors.stream().map(output -> output.map(List::of)).reduce(right(new ArrayList<>()), (x, y) -> {
            if (x.isRight() && y.isRight()) {
                return right(Stream.concat(x.get().stream(), y.get().stream()).toList());
            }
            if (x.isLeft() && y.isLeft()) {
                int problemAmount =
                        x.getLeft().problems().size() + y.getLeft().problems().size();
                return left(new FailedOperation(
                        String.format("%s problems %s", problemAmount, messageTemplate),
                        Stream.concat(x.getLeft().problems().stream(), y.getLeft().problems().stream())
                                .toList()));
            }
            return x.isLeft() ? x : y;
        });
    }
}
