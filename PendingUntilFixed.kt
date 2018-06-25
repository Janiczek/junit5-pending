import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import org.opentest4j.TestAbortedException

/**
 * The PendingUntilFixed annotation is intended for tests which currently fail
 * but will be fixed in the future (by implementing or fixing the tested code).
 *
 * If the test **fails**, the failure gets swallowed and we don't get a failing test.
 *
 * If the test **passes** (code got fixed?), we get an exception saying we should
 * remove the PendingUntilFixed annotation so that the test starts behaving
 * normally again and can fail "loudly" in the future.
 *
 * Usage:
 * ```
 * @PendingUntilFixed("RCRM-3943")
 * ```
 *
 * @see PendingUntilFixedExtension
 * @see PendingTestException
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ExtendWith(PendingUntilFixedExtension::class)
annotation class PendingUntilFixed(val jiraIssue: String)

private class PendingUntilFixedExtension : TestExecutionExceptionHandler, AfterTestExecutionCallback {

    enum class Flag {
        TEST_HAS_THROWN_EXCEPTION
    }

    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        // Swallow the exception, ie. don't throw it again; but make a note about it somewhere.
        context
            .getStore(Namespace.create(context.uniqueId))
            .put(Flag.TEST_HAS_THROWN_EXCEPTION, true)
    }

    override fun afterTestExecution(context: ExtensionContext) {

        val testHasThrownException: Boolean = context
            .getStore(Namespace.create(context.uniqueId))
            .getOrComputeIfAbsent(
                Flag.TEST_HAS_THROWN_EXCEPTION,
                { _ -> false }, // if the flag is absent, the test hasn't thrown an exception
                Boolean::class.java
            )

        // The test should throw an exception.
        // - If it does, we ignore it.
        // - If it doesn't, we throw an exception instead saying the annotation should get removed.

        if (testHasThrownException) {
            throw TestAbortedException()
        } else {
            throw PendingTestPassedException()
        }
    }
}

class PendingTestPassedException :
    Exception("Pending test passed, remove the `@PendingUntilFixed` annotation.")
