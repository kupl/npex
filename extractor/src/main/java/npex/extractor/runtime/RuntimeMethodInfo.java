package npex.extractor.runtime;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.utils.Misc;
import npex.extractor.invocation.MethodSignature;
import spoon.reflect.declaration.CtExecutable;


public class RuntimeMethodInfo {
	static Logger logger = LoggerFactory.getLogger(RuntimeMethodInfo.class);

	public static final String methodContextsPath = "/media/4tb/june/learning/rt/decompiled/methodContexts.ser";
  public static final String builderPatternsPath	= "/media/4tb/june/learning/rt/decompiled/builderPatterns.ser";

	public static Map<MethodSignature, Map<String, Boolean>> methodContextsMap = new HashMap<>();
	public static Map<String, Boolean> builderPatternsMap = new HashMap<>();

	static {
		if (new File(methodContextsPath).exists() && new File(builderPatternsPath).exists()) {
			deserialize();
		}
	}

  public static void serialize() {
		try {
			Misc.serialize(methodContextsMap, methodContextsPath);
			Misc.serialize(builderPatternsMap, builderPatternsPath);
		} catch (IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	private static void deserialize() {
		try {
			methodContextsMap = (Map) Misc.deserialize(methodContextsPath);
			builderPatternsMap = (Map) Misc.deserialize(builderPatternsPath);
		} catch (ClassNotFoundException | IOException e) {
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