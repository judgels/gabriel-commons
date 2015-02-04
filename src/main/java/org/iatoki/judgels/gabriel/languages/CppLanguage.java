package org.iatoki.judgels.gabriel.languages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.gabriel.Language;

import java.util.List;
import java.util.Set;

public final class CppLanguage extends Language {
    @Override
    public String getName() {
        return "C++";
    }

    @Override
    public Set<String> getAllowedExtensions() {
        return ImmutableSet.of("cpp", "cc");
    }

    @Override
    public String getExecutableFilename(String sourceFilename) {
        return sourceFilename.substring(0, sourceFilename.lastIndexOf('.'));
    }

    @Override
    public List<String> getCompilationCommand(String sourceFilename) {
        String executableFilename = getExecutableFilename(sourceFilename);
        return ImmutableList.of("/usr/bin/g++", "-o", executableFilename, sourceFilename);
    }

    @Override
    public List<String> getExecutionCommand(String sourceFilename) {
        String executableFilename = getExecutableFilename(sourceFilename);
        return ImmutableList.of("./" + executableFilename);
    }
}
