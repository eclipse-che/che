package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.RangeDTO;

import com.google.gwt.user.client.ui.Widget;

class CompletionItemBasedCompletionProposal implements CompletionProposal {

    private CompletionItemDTO completionItem;

    CompletionItemBasedCompletionProposal(CompletionItemDTO completionItem) {
        this.completionItem = completionItem;
    }

    @Override
    public Widget getAdditionalProposalInfo() {
        return null;
    }

    @Override
    public String getDisplayString() {
        return completionItem.getLabel();
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void getCompletion(CompletionCallback callback) {
        callback.onCompletion(new CompletionImpl(completionItem));
    }

    private static class CompletionImpl implements Completion {

        private CompletionItemDTO completionItem;

        public CompletionImpl(CompletionItemDTO completionItem) {
            this.completionItem = completionItem;
        }

        @Override
        public void apply(Document document) {
            RangeDTO range = completionItem.getTextEdit().getRange();
            int startOffset = document.getIndexFromPosition(
                    new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()));
            int endOffset = document
                    .getIndexFromPosition(new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));
            document.replace(startOffset, endOffset - startOffset, completionItem.getTextEdit().getNewText());
        }

        @Override
        public LinearRange getSelection(Document document) {
            RangeDTO range = completionItem.getTextEdit().getRange();
            int startOffset = document
                    .getIndexFromPosition(new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()))
                    + completionItem.getTextEdit().getNewText().length();
            return LinearRange.createWithStart(startOffset).andLength(0);
        }

    }

}
