package org.ffpy.plugin.coding.action.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class BaseIntentionAction implements IntentionAction {

    private static final String FAMILY_NAME = "Coding";
    private final String text;

    public BaseIntentionAction(String text) {
        if (StringUtils.isBlank(text)) throw new IllegalArgumentException("text不能为空");

        this.text = text;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }


    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
