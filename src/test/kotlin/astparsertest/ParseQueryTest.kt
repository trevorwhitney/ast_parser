package astparsertest

import arrow.core.Either
import astparser.DeploymentCondition
import astparser.JobCondition
import astparser.parseQuery
import org.assertj.core.api.Assertions.*
import org.junit.Test

class ParseQueryTest {
    @Test
    fun itReturnsAListOfConditions() {
        val results = parseQuery("job == 'foo' and deployment == 'bar'")
        when (results) {
            is Either.Right -> {
                assertThat(results.b).hasSize(2)
                assertThat(results.b).contains(JobCondition("foo"))
                assertThat(results.b).contains(DeploymentCondition("foo"))
            }
            else -> fail("#parseQuery should return a list of conditions")
        }
    }

    @Test
    fun itDoesNotSupportOtherOperations() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
