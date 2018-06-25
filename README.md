# junit5-pending
PendingUntilFixed for JUnit 5. Written in Kotlin.

## What is this?

The `PendingUntilFixed` annotation is intended for tests which currently fail
but will be fixed in the future (by implementing or fixing the tested code).

If the test **fails**, the failure gets swallowed and we don't get a failing test.

If the test **passes** (code got fixed?), we get an exception saying we should
remove the PendingUntilFixed annotation so that the test starts behaving
normally again and can fail "loudly" in the future.

### So, to recap:

The test should throw an exception.
- If it does, we ignore it.
- If it doesn't, we throw an exception instead saying the annotation should get removed.

## Usage

```kotlin
class MyTest {

    @Test
    @PendingUntilFixed("ISSUE-42")
    fun myTest() {

        // ...

    }

}
```
