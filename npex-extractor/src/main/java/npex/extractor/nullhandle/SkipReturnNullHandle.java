package npex.extractor.nullhandle;

import java.util.ArrayList;

import npex.common.NPEXException;
import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * This class is for collecting null constants. Null models collected with this
 * pattern do not used to learn null model classifiers.
 */
public class SkipReturnNullHandle extends AbstractNullHandle<CtIf> {
	CtExpression nullValueExpr;

	public SkipReturnNullHandle(CtIf handle, CtBinaryOperator nullCond, CtExpression nullValueExpr) {
		super(handle, nullCond);
		this.nullValueExpr = nullValueExpr;
	}

	public static SkipReturnNullHandle collect(CtIf handle) {
		CtExpression retExp = null;

		// Check if the then block consists of a single return statement.
		if (handle.getThenStatement()instanceof CtBlock blk && blk.getStatements().size() == 1) {
			if (blk.getStatement(0)instanceof CtReturn ret) {
				retExp = ret.getReturnedExpression();
			}
		}

		if (retExp == null)
			return null;

		if (handle.getCondition()instanceof CtBinaryOperator cond) {
			for (CtBinaryOperator bo : cond.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
				CtExpression nullExp = ASTUtils.findNullPointer(bo);
				if (nullExp == null) {
					continue;
				}
				return new SkipReturnNullHandle(handle, cond, retExp);
			}
		}

		return null;
	}

	/**
	 * TODO: We don't need this interface!
	 */
	@Override
	protected AbstractNullModelScanner createNullModelScanner(CtExpression nullExp) {
		return null;
	}

	// @Override
	// public void collectModels() throws NPEXException {
	// this.models = new ArrayList<>();
	// NullValue nullValue = NullValue.fromRawExpressionOnly(nullValueExpr);
	// this.models.add(new NullModel(nullExp, handle.getThenStatement(),
	// nullValue));
	// }

}