package npex.extractor.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtArrayWrite;

public class LHSIsArray implements Context {
	Logger logger = LoggerFactory.getLogger(LHSIsPublic.class);
	public Boolean extract(CtAbstractInvocation invo, int nullPos) {
		if (invo.getParent(CtArrayWrite.class) != null) {
			logger.info("{}: {}", invo, invo.getParent(CtArrayWrite.class));
		}
		return invo.getParent(CtArrayWrite.class) != null;
	}

}