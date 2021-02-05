/**
 * The MIT License
 *
 * Copyright (c) 2020 Software Analysis Laboratory, Korea University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package npex.synthesizer.template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.json.JSONObject;

import npex.common.utils.Misc;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

public class SourceChange<T extends CtElement> {
  private T before;
  private T after;
  private File sourceFile;

  private SourcePosition orgElementPosition;

  private ArrayList<String> linesPrinted = new ArrayList<>();

  private final String patchComment = "NPEX_PATCH_BEGINS";

  public SourceChange(T before, T after, CtElement additiveBegin) {
    this.before = before;
    this.after = after;
    this.after.setAnnotations(new ArrayList<>());
    this.orgElementPosition = before.getPosition();
    this.sourceFile = Misc.getSourceFile(this.before);

    /* Insert a comment to identify the begin of patch */
    CtComment comment = additiveBegin.getFactory().createComment(patchComment, CommentType.BLOCK);
    ArrayList<CtComment> comments = new ArrayList<>();
    comments.add(comment);
    additiveBegin.setComments(comments);
    assert (this.sourceFile.equals(Misc.getSourceFile(this.after)));
  }

  private ArrayList<String> getElementAfterSource() {
    String[] lines = this.after.toString().split(("\\r?\\n"));
    return new ArrayList<String>(Arrays.asList(lines));
  }

  private ArrayList<Integer> getPatchedLines() {
    assert (!linesPrinted.isEmpty());
    ArrayList<Integer> patchedLines = new ArrayList<Integer>();

    int lineNo = IntStream.range(0, linesPrinted.size()).filter(i -> linesPrinted.get(i).contains(patchComment))
        .findFirst().getAsInt();

    patchedLines.add(lineNo + 2);
    return patchedLines;
  }

  public void writeChangeToJson(String projectRootPath, File outFile) throws IOException {
    JSONObject json = new JSONObject();
    String relativeSourcePath = new File(projectRootPath).toURI().relativize(this.sourceFile.toURI()).getPath();
    json.put("original_filepath", relativeSourcePath);
    json.put("patched_lines", getPatchedLines());

    FileWriter writer = new FileWriter(outFile);
    writer.write(json.toString(4));
    writer.close();
  }

  public void writeChangeToSourceCode(File outFile) throws IOException {
    ArrayList<String> orgSourceLines = new ArrayList<>(Files.readAllLines(this.sourceFile.toPath()));
    ArrayList<String> afterLines = getElementAfterSource();
    int lineBegin = orgElementPosition.getLine();
    int lineEnd = orgElementPosition.getEndLine();

    orgSourceLines.subList(lineBegin - 1, lineEnd).clear();
    orgSourceLines.addAll(lineBegin - 1, afterLines);

    FileWriter writer = new FileWriter(outFile);
    for (String line : orgSourceLines) {
      writer.write(line + System.lineSeparator());
      this.linesPrinted.add(line);
    }

    writer.close();
  }

  void store(String projectRootPath, File outputDir) throws IOException {
    outputDir.mkdirs();
    writeChangeToSourceCode(new File(outputDir, "patch.java"));
    writeChangeToJson(projectRootPath, new File(outputDir, "patch.json"));
  }

}
