package fr.jukien.intellij.plugins.ui;

import javax.swing.*;

/**
 * Created on 24/04/2019
 *
 * @author JDI
 * @version 2.2.1
 * @since 1.0.0
 */
public class POJOGeneratorPanel {
    private JPanel panel;
    private JCheckBox capitalizeCheckBox;
    private JCheckBox schemaAttributeCheckBox;
    private JCheckBox generatedValueAnnotationsCheckBox;
    private JCheckBox manyToOneAndJoinColumnAnnotationsCheckBox;
    private JTextField prefixEntityTextField;
    private JTextField suffixEntityTextField;
    private JTextField prefixDTOTextField;
    private JTextField suffixDTOTextField;
    private JCheckBox alwaysShowDifferencesBetweenFilesCheckBox;

    public POJOGeneratorPanel(POJOGeneratorSettings pojoGeneratorSettings) {
        // Initialise l'interface graphique avec les settings qui ont été enregistrés avant
        capitalizeCheckBox.setSelected(pojoGeneratorSettings.getCapitalize());
        schemaAttributeCheckBox.setSelected(pojoGeneratorSettings.getWithSchemaAttribute());
        generatedValueAnnotationsCheckBox.setSelected(pojoGeneratorSettings.getAutoGenerated());
        manyToOneAndJoinColumnAnnotationsCheckBox.setSelected(pojoGeneratorSettings.getWithRelationshipAnnotations());
        prefixDTOTextField.setText(pojoGeneratorSettings.getPrefixDto());
        suffixDTOTextField.setText(pojoGeneratorSettings.getSuffixDto());
        prefixEntityTextField.setText(pojoGeneratorSettings.getPrefixEntity());
        suffixEntityTextField.setText(pojoGeneratorSettings.getSuffixEntity());
        alwaysShowDifferencesBetweenFilesCheckBox.setSelected(pojoGeneratorSettings.getAlwaysShowDifferencesBetweenFiles());
    }

    public JPanel getPanel() {
        return panel;
    }

    public JCheckBox getCapitalizeCheckBox() {
        return capitalizeCheckBox;
    }

    public JCheckBox getSchemaAttributeCheckBox() {
        return schemaAttributeCheckBox;
    }

    public JCheckBox getGeneratedValueAnnotationsCheckBox() {
        return generatedValueAnnotationsCheckBox;
    }

    public JCheckBox getManyToOneAndJoinColumnAnnotationsCheckBox() {
        return manyToOneAndJoinColumnAnnotationsCheckBox;
    }

    public JTextField getPrefixEntityTextField() {
        return prefixEntityTextField;
    }

    public JTextField getSuffixEntityTextField() {
        return suffixEntityTextField;
    }

    public JTextField getPrefixDTOTextField() {
        return prefixDTOTextField;
    }

    public JTextField getSuffixDTOTextField() {
        return suffixDTOTextField;
    }

    public JCheckBox getAlwaysShowDifferencesBetweenFilesCheckBox() {
        return alwaysShowDifferencesBetweenFilesCheckBox;
    }
}
