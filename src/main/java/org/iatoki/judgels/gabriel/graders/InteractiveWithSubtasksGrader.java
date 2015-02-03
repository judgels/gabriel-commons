package org.iatoki.judgels.gabriel.graders;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.gabriel.GradingConfig;
import org.iatoki.judgels.gabriel.GradingLanguage;
import org.iatoki.judgels.gabriel.Language;
import org.iatoki.judgels.gabriel.LanguageRegistry;
import org.iatoki.judgels.gabriel.Sandbox;
import org.iatoki.judgels.gabriel.SandboxFactory;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGrader;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingConfig;
import org.iatoki.judgels.gabriel.blackbox.Compiler;
import org.iatoki.judgels.gabriel.blackbox.Evaluator;
import org.iatoki.judgels.gabriel.blackbox.PreparationException;
import org.iatoki.judgels.gabriel.blackbox.Reducer;
import org.iatoki.judgels.gabriel.blackbox.Scorer;
import org.iatoki.judgels.gabriel.blackbox.TestGroup;
import org.iatoki.judgels.gabriel.blackbox.algorithms.IdentitySubtaskScorer;
import org.iatoki.judgels.gabriel.blackbox.algorithms.InteractiveEvaluator;
import org.iatoki.judgels.gabriel.blackbox.algorithms.SingleSourceFileCompiler;
import org.iatoki.judgels.gabriel.blackbox.algorithms.SubtaskReducer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class InteractiveWithSubtasksGrader extends BlackBoxGrader {
    private Compiler compiler;
    private Evaluator evaluator;
    private Scorer scorer;
    private Reducer reducer;

    private Sandbox compilerSandbox;
    private Sandbox evaluatorContestantSandbox;
    private Sandbox evaluatorCommunicatorSandbox;

    @Override
    public String getName() {
        return "Interactive with Subtasks";
    }

    @Override
    protected void prepare(SandboxFactory sandboxFactory, File workingDir, BlackBoxGradingConfig config, Language language, Map<String, File> sourceFiles, Map<String, File> helperFiles) throws PreparationException {
        InteractiveWithSubtasksGradingConfig thisConfig = (InteractiveWithSubtasksGradingConfig) config;
        if (thisConfig.getCommunicator() == null) {
            throw new PreparationException("Communicator not specified");
        }
        File contestantSourceFile = sourceFiles.get("source");
        File communicatorSourceFile = helperFiles.get(thisConfig.getCommunicator());

        File compilationDir;
        File evaluationDir;
        File scoringDir;

        try {
            compilationDir = new File(workingDir, "compilation");
            FileUtils.forceMkdir(compilationDir);
            evaluationDir = new File(workingDir, "evaluation");
            FileUtils.forceMkdir(evaluationDir);
            scoringDir = new File(workingDir, "scoring");
            FileUtils.forceMkdir(scoringDir);
        } catch (IOException e) {
            throw new PreparationException("Cannot make directories inside " + workingDir.getAbsolutePath());
        }

        compilerSandbox = sandboxFactory.newSandbox();
        compiler = new SingleSourceFileCompiler(compilerSandbox, compilationDir, language, contestantSourceFile, 10000, 100 * 1024);

        evaluatorContestantSandbox = sandboxFactory.newSandbox();
        evaluatorCommunicatorSandbox = sandboxFactory.newSandbox();

        Language cppLanguage = LanguageRegistry.getInstance().getLanguage(GradingLanguage.CPP);

        evaluator = new InteractiveEvaluator(evaluatorContestantSandbox, evaluatorCommunicatorSandbox, compilationDir, evaluationDir, language, cppLanguage, contestantSourceFile, communicatorSourceFile,  10000, 100 * 1024, thisConfig.getTimeLimitInMilliseconds(), thisConfig.getMemoryLimitInKilobytes());
        scorer = new IdentitySubtaskScorer(evaluationDir);
        reducer = new SubtaskReducer();
    }

    @Override
    protected Compiler getCompiler() {
        return compiler;
    }

    @Override
    protected Evaluator getEvaluator() {
        return evaluator;
    }

    @Override
    protected Scorer getScorer() {
        return scorer;
    }

    @Override
    protected Reducer getReducer() {
        return reducer;
    }

    @Override
    public GradingConfig createDefaultGradingConfig() {
        InteractiveWithSubtasksGradingConfig config = new InteractiveWithSubtasksGradingConfig();
        config.timeLimitInMilliseconds = 2000;
        config.memoryLimitInKilobytes = 65536;
        config.testData = ImmutableList.of(new TestGroup(0, ImmutableList.of()));

        ImmutableList.Builder<Integer> subtaskPoints = ImmutableList.builder();
        for (int i = 0; i < 10; i++) {
            subtaskPoints.add(0);
        }
        config.subtaskPoints = subtaskPoints.build();

        return config;
    }

    @Override
    public GradingConfig createGradingConfigFromJson(String json) {
        return new Gson().fromJson(json, InteractiveWithSubtasksGradingConfig.class);
    }

    @Override
    public void cleanUp() {
        compilerSandbox.cleanUp();
        evaluatorContestantSandbox.cleanUp();
        evaluatorCommunicatorSandbox.cleanUp();
    }
}
