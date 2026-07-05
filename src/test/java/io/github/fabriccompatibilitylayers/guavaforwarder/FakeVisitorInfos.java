package io.github.fabriccompatibilitylayers.guavaforwarder;

import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.VisitorInfos;

import java.util.ArrayList;
import java.util.List;

/** Test double recording every {@link #registerMethodInvocation} call it receives. */
public class FakeVisitorInfos implements VisitorInfos {
    public record MethodRedirect(String targetClass, String targetMethod, String targetDesc,
                                  String replacementOwner, String replacementName, String replacementDesc,
                                  Boolean replacementStatic) {
    }

    public final List<MethodRedirect> methodRedirects = new ArrayList<>();

    @Override
    public void registerSuperType(String target, String replacement) {
    }

    @Override
    public void registerTypeAnnotation(String target, String replacement) {
    }

    @Override
    public void registerMethodTypeIns(String target, String replacement) {
    }

    @Override
    public void registerFieldRef(String targetClass, String targetField, String targetDesc, FullClassMember classMember) {
    }

    @Override
    public void registerMethodInvocation(String targetClass, String targetMethod, String targetDesc, FullClassMember classMember) {
        methodRedirects.add(new MethodRedirect(
                targetClass, targetMethod, targetDesc,
                classMember.getOwner(), classMember.getName(), classMember.getDesc(), classMember.isStatic()
        ));
    }

    @Override
    public void registerLdc(String targetClass, Object targetLdc, Object replacement) {
    }

    @Override
    public void registerInstantiation(String target, String replacement) {
    }
}
