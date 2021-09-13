package npex.extractor.context;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtAbstractInvocation;

public class NameContext implements Context {
	private final String name;
	private final String keyword;

	private static final String[] frequentNames = {
		"get", "is", "set", "equals", "Code", "hash", "close", "Empty", "add", "Value",
		"String", "to", "contains", "Name", "remove", "<init>", "write", "Class", "clone", "stop"
	};



	public static final List<NameContext> all = new ArrayList<>();
	static {
		for (String name : frequentNames) {
			all.add(new NameContext(name));
		}
	}

	public NameContext(String name) {
		this.name = String.format("%sInMethodName", name);
		this.keyword = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public Boolean extract(CtAbstractInvocation invo, int nullPos) {
		return invo.getExecutable().getSimpleName().contains(keyword);
	}

}