package org.ffpy.plugin.coding.ui.form;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.ffpy.plugin.coding.constant.CommentPosition;
import org.ffpy.plugin.coding.ui.utils.InputLimit;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class XmlToBeanForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField packageName;
    private JComboBox<String> commentPosition;
    private JTextArea text;
    private JLabel tip;

    private Action action;

    public XmlToBeanForm(@Nullable String text) {
        setTitle("XML转Bean");

        setSize(800, 900);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        initComponent();

        if (StringUtils.isNotEmpty(text)) {
            setText(text);
            EventQueue.invokeLater(() -> packageName.requestFocus());
        } else {
            EventQueue.invokeLater(() -> this.text.requestFocus());
        }

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getText() {
        return text.getText();
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public String getPackageName() {
        return packageName.getText();
    }

    public void setPackageName(String packageName) {
        this.packageName.setText(packageName);
    }

    public void setTip(String tip) {
        this.tip.setText(tip);
    }

    private void initComponent() {
        for (CommentPosition position : CommentPosition.values()) {
            commentPosition.addItem(position.getName());
        }

        new InputLimit(packageName, "^[a-z_]+(\\.[a-z0-9_]*)*$");
    }

    private void onOK() {
        String text = getText();
        if (text.isEmpty()) {
            setTip("XML内容不能为空");
            this.text.requestFocus();
            return;
        }

        Document doc;
        try {
            doc = DocumentHelper.parseText(text);
        } catch (DocumentException e) {
            setTip("XML内容格式不正确");
            this.text.requestFocus();
            return;
        }

        String packageName = getPackageName();
        if (packageName.isEmpty()) {
            setTip("包名不能为空");
            this.packageName.requestFocus();
            return;
        }

        action.onOk(doc, packageName, CommentPosition.values()[commentPosition.getSelectedIndex()]);

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public interface Action {
        /**
         * 按下了确定按钮
         *
         * @param doc         XML文档
         * @param packageName 包名
         * @param position    注释位置
         */
        void onOk(Document doc, String packageName, CommentPosition position);
    }

}
