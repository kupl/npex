package npex.extractor.runtime;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.NPEXException;
import npex.extractor.context.Context;
import npex.extractor.context.ContextFactory;
import npex.extractor.invocation.MethodSignature;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

public class RuntimeMethodInfoProcessor extends AbstractProcessor<CtExecutable> {
	static Logger logger = LoggerFactory.getLogger(RuntimeMethodInfoProcessor.class);
	static private List<Context> calleeContexts = ContextFactory.getCalleeContexts();

	final private Map<MethodSignature, Map<String, Boolean>> methodContextsMap = RuntimeMethodInfo.methodContextsMap;
	final private Map<String, Boolean> builderPatternsMap = RuntimeMethodInfo.builderPatternsMap;
	final private File resultsOut;

	public RuntimeMethodInfoProcessor(String resultsPath) {
		super();
		this.resultsOut = new File(resultsPath);
	}

	@Override
	public boolean isToBeProcessed(CtExecutable exec) {
		return exec instanceof CtMethod || exec instanceof CtConstructor;
	}

	@Override
	public void process(CtExecutable exec) {
		int begin = exec.getReference().isStatic() ? 0 : -1;
		if (begin == -1) {
			List<CtReturn> returns = exec.getElements(new TypeFilter<>(CtReturn.class));
			Predicate<CtReturn> returnsThis = ret -> {
				CtExpression rexp = ret.getReturnedExpression();
				return rexp != null && rexp.toString().equals("this");
			};
			boolean isBuilderPattern = !returns.isEmpty() && returns.stream().allMatch(returnsThis);
			builderPatternsMap.put(exec.getSignature(), isBuilderPattern);
		}
 
		for (int nullPos = begin; nullPos < exec.getParameters().size(); nullPos++) {
			try {
				MethodSignature sig = new MethodSignature(exec, nullPos);
				var contexts = new HashMap<String, Boolean>();
				for (Context ctx : calleeContexts) {
					contexts.put(ctx.getName(), ctx.extract(exec, nullPos));
				}
				methodContextsMap.put(sig, contexts);
			} catch (NPEXException e) {
				logger.error(e.getMessage());
				continue;
			}
		}
	}

	@Override
	public void processingDone() {
			RuntimeMethodInfo.serialize();
	}

}