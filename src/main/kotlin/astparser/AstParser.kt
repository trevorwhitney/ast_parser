package astparser

import arrow.core.Either
import arrow.core.flatMap
import org.springframework.expression.spel.SpelNode
import org.springframework.expression.spel.ast.*
import org.springframework.expression.spel.standard.SpelExpressionParser

fun parseQuery(query: String): Either<ParseError, List<Condition>> {
    val spelExpressionParser = SpelExpressionParser()
    val spelExpression = spelExpressionParser.parseRaw(query)
    return traverseAst(spelExpression.ast)
}

fun traverseAst(ast: SpelNode): Either<ParseError, List<Condition>> {
    return when (ast) {
        is OpEQ -> Condition
            .fromSpelNodes(ast.leftOperand, ast.rightOperand)
            .flatMap { Either.right(listOf(it)) }

        is OpAnd -> traverseAst(ast.leftOperand).flatMap({ leftConditions: List<Condition> ->
            traverseAst(ast.rightOperand).flatMap { rightConditions: List<Condition> ->
                Either.right(leftConditions.union(rightConditions).toList())
            }
        })

        else -> Either.left(UnsupportedExpressionError())
    }
}

sealed class Condition(val name: String) {
    companion object {
        fun fromSpelNodes(left: SpelNodeImpl, right: SpelNodeImpl): Either<ParseError, Condition> {
            if (left !is PropertyOrFieldReference) return Either.left(UnsupportedExpressionError())
            if (right !is StringLiteral) return Either.left(UnsupportedExpressionError())

            val value = right.originalValue
            if (value === null) return Either.left(UnsupportedExpressionError())

            return when (left.name) {
                "job" -> Either.right(JobCondition(value))
                "deployment" -> Either.right(DeploymentCondition(value))
                "metric" -> Either.right(MetricCondition(value))
                "origin" -> Either.right(OriginCondition(value))
                else -> Either.left(UnsupportedConditionError())
            }
        }
    }
}

class JobCondition(name: String) : Condition(name) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class DeploymentCondition(name: String) : Condition(name) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class MetricCondition(name: String) : Condition(name) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class OriginCondition(name: String) : Condition(name) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

open class ParseError
class UnsupportedConditionError : ParseError()
class UnsupportedExpressionError : ParseError()
