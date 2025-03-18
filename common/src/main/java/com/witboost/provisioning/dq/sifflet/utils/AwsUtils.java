package com.witboost.provisioning.dq.sifflet.utils;

import com.witboost.provisioning.model.common.FailedOperation;
import io.vavr.control.Either;

public class AwsUtils {

    public static Either<FailedOperation, String> convertArn(String inputArn) {
        if (inputArn == null || !inputArn.startsWith("arn:aws:sts")) {
            throw new IllegalArgumentException("Invalid input ARN");
        }

        String[] parts = inputArn.split(":");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid ARN format");
        }

        String accountId = parts[4];
        String rolePart = parts[5].replace("assumed-role/", "").split("/")[0];

        return Either.right("arn:aws:iam::" + accountId + ":role/" + rolePart);
    }
}
