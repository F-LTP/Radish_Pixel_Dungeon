package com.shatteredpixel.shatteredpixeldungeon.events.processor;

import com.google.auto.service.AutoService;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Annotation processor for @SubscribeEvent.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EventSubscriberProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    private static final String USAGE_GUIDE =
        "Correct usage:\n" +
        "  @SubscribeEvent(event = YourEvent.class, priority = 0)\n" +
        "  public static void onEvent(YourEvent event) { ... }\n\n" +
        "Requirements:\n" +
        "  - Method must be static\n" +
        "  - Method must have exactly one parameter\n" +
        "  - Parameter type must match the event type";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, List<SubscriberInfo>> subscribersByEvent = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(SubscribeEvent.class)) {
            if (element instanceof ExecutableElement) {
                processElement((ExecutableElement) element, subscribersByEvent);
            }
        }

        if (!subscribersByEvent.isEmpty()) {
            try {
                EventSubscriberIndexGenerator.generate(subscribersByEvent, filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate index: " + e.getMessage());
            }
        }

        return true;
    }

    private void processElement(ExecutableElement method, Map<String, List<SubscriberInfo>> subscribersByEvent) {
        TypeMirror eventType = getAnnotationValue(method, "event");
        int priority = getAnnotationValue(method, "priority", 0);

        boolean notStatic = !method.getModifiers().contains(Modifier.STATIC);
        boolean wrongParamCount = method.getParameters().size() != 1;
        boolean noEventType = eventType == null;
        boolean typeMismatch = eventType != null && method.getParameters().size() == 1
            && !processingEnv.getTypeUtils().isAssignable(method.getParameters().get(0).asType(), eventType);

        if (notStatic || wrongParamCount || noEventType || typeMismatch) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                "Invalid @SubscribeEvent usage for method '" + method.getSimpleName() + "'\n\n" + USAGE_GUIDE, method);
            return;
        }

        TypeElement enclosingClass = (TypeElement) method.getEnclosingElement();
        String className = enclosingClass.getQualifiedName().toString();
        String methodName = method.getSimpleName().toString();
        String eventTypeName = eventType.toString();

        SubscriberInfo info = new SubscriberInfo(className, methodName, eventTypeName, priority);
        subscribersByEvent.computeIfAbsent(eventTypeName, k -> new ArrayList<>()).add(info);
    }

    private TypeMirror getAnnotationValue(ExecutableElement method, String key) {
        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals("com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent")) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                    if (key.equals(entry.getKey().getSimpleName().toString())) {
                        return (TypeMirror) entry.getValue().getValue();
                    }
                }
            }
        }
        return null;
    }

    private int getAnnotationValue(ExecutableElement method, String key, int defaultValue) {
        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals("com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent")) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                    if (key.equals(entry.getKey().getSimpleName().toString())) {
                        return (Integer) entry.getValue().getValue();
                    }
                }
            }
        }
        return defaultValue;
    }
}