import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;

public class Setter extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        //获取当前事件触发时，光标所在的元素
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        PsiClassImpl psiType = (PsiClassImpl) psiElement;
        // 获取类名称
        String className = psiType.getName();
        System.out.println("类名:" + className);
        // 转换成变量
        String valiableClassName =  (new StringBuilder()).append(Character.toLowerCase(className.charAt(0))).append(className.substring(1)).toString();
        System.out.println("成员变量名:" + valiableClassName);
        PsiMethod[] parentMethods = psiType.getAllMethods();
        // 循环实现父类方法
        final Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int start = primaryCaret.getVisualLineEnd();
        // De-select the text range that was just replaced
        primaryCaret.removeSelection();
        StringBuffer buffer = new StringBuffer();
        for (PsiMethod method : parentMethods) {
            // 方法名称
            String methodName = method.getName();
            // 判断是否是set方法
            if (methodName.startsWith("set")) {
                System.out.println("set方法名称:" + methodName);
                // 变量名称
                String filedName = methodName.replace("set", "");
                System.out.println("参数名称:"+filedName);
                // 转换参数名称
                String convertFiledName = (new StringBuilder()).append(Character.toLowerCase(filedName.charAt(0))).append(filedName.substring(1)).toString();
                System.out.println("转换后参数名称:"+convertFiledName);
                buffer.append("        "+valiableClassName + "." + methodName + "(" + convertFiledName + ");");
                buffer.append("\n");
            }
        }
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.insertString(start, buffer.toString())
        );

    }

    //psiFile转psiClass
    public static PsiClass getPsiClass(PsiFile psiFile) {
        String fullName = psiFile.getName();
        String className = fullName.split("\\.")[0];
        PsiClass[] psiClasses = PsiShortNamesCache.getInstance(psiFile.getProject()).getClassesByName(className, new EverythingGlobalScope(psiFile.getProject()));
        return psiClasses[0];
    }
}
