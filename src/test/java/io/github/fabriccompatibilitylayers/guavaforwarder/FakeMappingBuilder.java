package io.github.fabriccompatibilitylayers.guavaforwarder;

import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingBuilder;

import java.util.ArrayList;
import java.util.List;

/** Test double recording every rename registered through it. */
public class FakeMappingBuilder implements MappingBuilder {
    public record ClassRename(String from, String to) {
    }

    public record MethodRename(String owner, String from, String to, String descriptor) {
    }

    public final List<ClassRename> classRenames = new ArrayList<>();
    public final List<MethodRename> methodRenames = new ArrayList<>();

    @Override
    public ClassMapping addMapping(String sourceName, String targetName) {
        classRenames.add(new ClassRename(sourceName, targetName));
        return new Recorder(sourceName);
    }

    @Override
    public ClassMapping addMapping(String name) {
        return new Recorder(name);
    }

    private class Recorder implements ClassMapping {
        private final String owner;

        private Recorder(String owner) {
            this.owner = owner;
        }

        @Override
        public ClassMapping field(String sourceName, String targetName, String sourceDescriptor) {
            return this;
        }

        @Override
        public ClassMapping field(String name, String descriptor) {
            return this;
        }

        @Override
        public ClassMapping method(String sourceName, String targetName, String sourceDescriptor) {
            methodRenames.add(new MethodRename(owner, sourceName, targetName, sourceDescriptor));
            return this;
        }

        @Override
        public ClassMapping method(String name, String descriptor) {
            return this;
        }
    }
}
