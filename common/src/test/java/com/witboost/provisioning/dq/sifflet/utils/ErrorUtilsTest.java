package com.witboost.provisioning.dq.sifflet.utils;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import io.vavr.control.Either;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ErrorUtilsTest {

    @Test
    public void testMergeSequence_AllRight() {
        List<Either<FailedOperation, String>> errors = List.of(right("value1"), right("value2"), right("value3"));

        Either<FailedOperation, List<String>> result = ErrorUtils.mergeSequence(errors, "All right");

        assertTrue(result.isRight(), "Result should be right");
        assertEquals(List.of("value1", "value2", "value3"), result.get());
    }

    @Test
    public void testMergeSequence_SomeLeft() {
        FailedOperation failedOperation1 = new FailedOperation("Error 1", List.of(new Problem("Problem 1")));
        FailedOperation failedOperation2 = new FailedOperation("Error 2", List.of(new Problem("Problem 2")));

        List<Either<FailedOperation, String>> errors =
                List.of(right("value1"), left(failedOperation1), right("value2"), left(failedOperation2));

        Either<FailedOperation, List<String>> result = ErrorUtils.mergeSequence(errors, "Some lefts");

        assertTrue(result.isLeft(), "Result should be left");
        FailedOperation failedOperation = result.getLeft();
        assertEquals("2 problems Some lefts", failedOperation.message());
        assertEquals(2, failedOperation.problems().size());
        assertTrue(failedOperation.problems().get(0).getMessage().contains("Problem 1"));
        assertTrue(failedOperation.problems().get(1).getMessage().contains("Problem 2"));
    }

    @Test
    public void testMergeSequence_AllLeft() {
        FailedOperation failedOperation1 = new FailedOperation("Error 1", List.of(new Problem("Problem 1")));
        FailedOperation failedOperation2 = new FailedOperation("Error 2", List.of(new Problem("Problem 2")));

        List<Either<FailedOperation, String>> errors = List.of(left(failedOperation1), left(failedOperation2));

        Either<FailedOperation, List<String>> result = ErrorUtils.mergeSequence(errors, "All lefts");

        assertTrue(result.isLeft(), "Result should be left");
        FailedOperation failedOperation = result.getLeft();
        assertEquals("2 problems All lefts", failedOperation.message());
        assertEquals(2, failedOperation.problems().size());
        assertTrue(failedOperation.problems().get(0).getMessage().contains("Problem 1"));
        assertTrue(failedOperation.problems().get(1).getMessage().contains("Problem 2"));
    }

    @Test
    public void testMergeSequence_SingleRight() {
        List<Either<FailedOperation, String>> errors = List.of(right("value1"));

        Either<FailedOperation, List<String>> result = ErrorUtils.mergeSequence(errors, "Single right");

        assertTrue(result.isRight(), "Result should be right");
        assertEquals(List.of("value1"), result.get());
    }

    @Test
    public void testMergeSequence_SingleLeft() {
        FailedOperation failedOperation = new FailedOperation("Error 1", List.of(new Problem("Problem 1")));
        List<Either<FailedOperation, String>> errors = List.of(left(failedOperation));

        Either<FailedOperation, List<String>> result = ErrorUtils.mergeSequence(errors, "Single left");

        assertTrue(result.isLeft(), "Result should be left");
        FailedOperation failedOperationResult = result.getLeft();
        assertEquals("Error 1", failedOperationResult.message());
        assertEquals(1, failedOperationResult.problems().size());
        assertTrue(failedOperationResult.problems().get(0).getMessage().contains("Problem 1"));
    }
}
