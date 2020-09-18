package npex.errortracer;

import org.apache.log4j.Logger;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

public abstract class AbstractLoggerProcessor<E extends CtElement> extends AbstractProcessor<E> {
  protected Logger logger = Logger.getLogger(AbstractLoggerProcessor.class);
  final String entryTag;

  AbstractLoggerProcessor(String entryTag) {
    super();
    this.entryTag = entryTag;
  }

  public boolean isToBeProcessed(E e) {
    return e.getPosition().isValidPosition() && _isToBeProcessed(e);
  }

  CtStatement createPrintStatement(E e) {
    SourcePosition pos = e.getPosition();
    CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
    String arguments = String.format("[%s] Filepath: %s, Line: %d, Element: %s", entryTag, pos.getFile(), pos.getLine(),
        getElementName(e));
    snippet.setValue(String.format("System.out.println(\"%s\")", arguments));
    logger.info(arguments);
    return snippet;
  }

  abstract String getElementName(E e);

  abstract boolean _isToBeProcessed(E e);
}