package npex.extractor.runtime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.Configs;
import npex.common.NPEXException;
import npex.common.utils.Misc;
import npex.extractor.invocation.MethodSignature;
import spoon.reflect.declaration.CtExecutable;

public class RuntimeMethodInfo {
	static Logger logger = LoggerFactory.getLogger(RuntimeMethodInfo.class);

	public static Map<MethodSignature, Map<String, Boolean>> methodContextsMap;
	public static Map<String, Boolean> builderPatternsMap;

	static {
		try {
			methodContextsMap = (Map) Misc.deserialize(
					RuntimeMethodInfo.class.getResourceAsStream("/" + Configs.javaRuntimeMethodContextsName));
			builderPatternsMap = (Map) Misc
					.deserialize(RuntimeMethodInfo.class.getResourceAsStream("/" + Configs.builderPatternsName));
			assert (methodContextsMap.size() > 0 && builderPatternsMap.size() > 0);
		} catch (java.lang.ClassNotFoundException | java.io.IOException e) {
			throw new NPEXException("Could not deserialize runtime method info. This should not happen!", e.getCause());
		}

	}

	public static void serialize() {
		try {
			Misc.serialize(methodContextsMap, Configs.javaRuntimeMethodContextsName);
			Misc.serialize(builderPatternsMap, Configs.builderPatternsName);
		} catch (IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	public static boolean isRuntimeCallee(MethodSignature sig) {
		return methodContextsMap.containsKey(sig);
	}

	public static Map<String, Boolean> getContexts(MethodSignature sig) {
		return methodContextsMap.get(sig);
	}

	public static boolean hasCalleeBuilderPatterns(CtExecutable exec) {
		return Boolean.TRUE.equals(builderPatternsMap.get(exec.getSignature()));
	}
}