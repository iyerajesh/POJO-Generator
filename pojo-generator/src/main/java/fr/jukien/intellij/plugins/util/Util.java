package fr.jukien.intellij.plugins.util;

import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasForeignKey;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.vcsUtil.VcsUtil;
import com.twelvemonkeys.util.LinkedSet;
import fr.jukien.intellij.plugins.ui.ConfigurableJPAMapping;
import fr.jukien.intellij.plugins.ui.DBMSFamily;
import fr.jukien.intellij.plugins.ui.JPAMappingSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 25/04/2019
 *
 * @author JDI
 * @version 2.0.0
 * @since 1.0.0
 */
public class Util {
    public static VirtualFile lastChoosedFile;

    public static LinkedSet<Field> getFields(DbTable dbTable, JPAMappingSettings jpaMappingSettings) {
        LinkedSet<Field> fields = new LinkedSet<>();
        List<ConfigurableJPAMapping> jpaMappings = jpaMappingSettings.getJpaMappings();
        for (DasColumn column : DasUtil.getColumns(dbTable)) {
            Field field = new Field();
            field.setName(column.getName());
            field.setAutoGenerated(DasUtil.isAutoGenerated(column));
            field.setSQLType(column.getDataType());

            ConfigurableJPAMapping configurableJPAMapping = jpaMappings.stream()
                    .filter(c -> dbTable.getDataSource().getDatabaseVersion().name.equals(c.getFamily().getName()) &&
                            column.getDataType().typeName.equals(c.getSqlDataType()))
                    .findAny()
                    .orElse(null);

            if (null != configurableJPAMapping) {
                field.setJavaType(configurableJPAMapping.getJavaDataType());
                field.setColumnDefinition(configurableJPAMapping.getJavaColumnDefinition());
            } else {
                field.setJavaType(null);
                field.setColumnDefinition(null);
            }
            field.setPrimary(DasUtil.isPrimary(column));
            fields.add(field);
        }

        for (DasForeignKey dasForeignKey : DasUtil.getForeignKeys(dbTable)) {
            dasForeignKey.getColumnsRef().names().forEach(s -> {
                fields.forEach(field -> {
                    if (field.getName().equals(s)) {
                        field.setForeignKey(true);
                    }
                });
            });
        }
        return fields;
    }

    public static void addGetterSetter(Set<Field> fields, StringBuilder javaTextFile) {
        for (Field field : fields) {
            javaTextFile.append("\n");

            javaTextFile.append("    public ").append(field.getJavaType()).append(" get").append(javaName(field.getName(), true)).append("() {").append("\n");
            javaTextFile.append("        return this.").append(javaName(field.getName(), false)).append(";").append("\n");
            javaTextFile.append("    }").append("\n");

            javaTextFile.append("\n");

            javaTextFile.append("    public void set").append(javaName(field.getName(), true)).append("(").append(field.getJavaType()).append(" ").append(javaName(field.getName(), false)).append(") {").append("\n");
            javaTextFile.append("        this.").append(javaName(field.getName(), false)).append(" = ").append(javaName(field.getName(), false)).append(";").append("\n");
            javaTextFile.append("    }").append("\n");
        }

        javaTextFile.append("}").append("\n");
    }

    public static String javaName(String str, Boolean capitalizeFirstLetter) {
        String[] strings = NameUtil.splitNameIntoWords(str);
        StringBuilder name = new StringBuilder();

        for (int i = 0; strings.length > i; i++) {
            if (i == 0) {
                if (capitalizeFirstLetter) {
                    name.append(convertToTitleCaseIteratingChars(strings[i]));
                } else {
                    name.append(strings[i].toLowerCase());
                }
            } else {
                name.append(convertToTitleCaseIteratingChars(strings[i]));
            }
        }
        return name.toString();
    }

    public static String convertToTitleCaseIteratingChars(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

    public static boolean isDatabaseSupported(PsiElement[] psiElements) {
        if (psiElements[0] instanceof DbTable) {
            return Util.getDatabases().contains(((DbTable) psiElements[0]).getDataSource().getDatabaseVersion().name);
        } else {
            return false;
        }
    }

    public static void checkActionVisibility(@NotNull AnActionEvent anActionEvent, String actionText) {
        final Project project = anActionEvent.getProject();
        if (null == project) {
            return;
        }

        PsiElement[] psiElements = anActionEvent.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements == null || psiElements.length == 0) {
            return;
        }

        if (psiElements[0] instanceof DbTable) {
            if (isDatabaseSupported(psiElements)) {
                anActionEvent.getPresentation().setEnabled(true);
            } else {
                anActionEvent.getPresentation().setEnabled(false);
                anActionEvent.getPresentation().setText(String.format("%s : database not supported", actionText));
            }
        } else {
            anActionEvent.getPresentation().setEnabled(false);
            anActionEvent.getPresentation().setText(String.format("%s : please, select a table", actionText));
        }
    }

    public static Set<String> getDatabases() {
        DBMSFamily[] dbmsFamilies = DBMSFamily.values();
        Set<String> names = new HashSet<>();
        for (DBMSFamily dbmsFamily : dbmsFamilies) {
            names.add(dbmsFamily.getName());
        }
        return names;
    }

    /**
     * Create file or show the differences if the file already exist
     *
     * @param project
     * @param javaTextFile
     * @param fileName
     * @since 2.1.0
     */
    public static void createFile(Project project, StringBuilder javaTextFile, String fileName) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName, JavaClassFileType.INSTANCE, javaTextFile);
        PsiDirectory psiDirectory = PsiDirectoryFactory.getInstance(project).createDirectory(lastChoosedFile);

        if (null == psiDirectory.findFile(file.getName())) {
            Runnable r = () -> psiDirectory.add(file);

            WriteCommandAction.runWriteCommandAction(project, r);
        } else {
            Notification notification = new Notification("POJO Generator", "POJO Generator", String.format("The file [%s] already exists", fileName), NotificationType.WARNING, null);
            notification.addAction(NotificationAction.createSimple("Show Diff...", () -> {
                notification.expire();

                VirtualFile existingFile = psiDirectory.findFile(file.getName()).getVirtualFile();

                DiffContentFactory contentFactory = DiffContentFactory.getInstance();
                DiffRequestFactory requestFactory = DiffRequestFactory.getInstance();

                DiffContent generatedFileContent = contentFactory.create(project, file.getText());
                DiffContent existingFileContent = contentFactory.create(project, existingFile);

                MutableDiffRequestChain chain = new MutableDiffRequestChain(generatedFileContent, existingFileContent);

                chain.setWindowTitle(String.format("%s vs %s", "Generated file", VcsUtil.getFilePath(existingFile).getName()));
                chain.setTitle1("Generated file");
                chain.setTitle2(requestFactory.getContentTitle(existingFile));

                DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT);
            }));
            Notifications.Bus.notify(notification, project);
        }
    }
}
