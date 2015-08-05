package org.iatoki.judgels.gabriel.blackbox.algorithms;

import com.google.common.collect.Lists;
import org.iatoki.judgels.gabriel.blackbox.EvaluationVerdict;
import org.iatoki.judgels.gabriel.blackbox.NormalVerdict;
import org.iatoki.judgels.gabriel.blackbox.Reducer;
import org.iatoki.judgels.gabriel.blackbox.ReductionResult;
import org.iatoki.judgels.gabriel.blackbox.ReductionVerdict;
import org.iatoki.judgels.gabriel.blackbox.ScoringVerdict;
import org.iatoki.judgels.gabriel.blackbox.SubtaskResult;

import java.util.Collections;
import java.util.List;

public abstract class AbstractReducer implements Reducer {
    @Override
    public ReductionResult reduceSubtaskResults(List<SubtaskResult> subtaskResults) {
        if (subtaskResults.size() == 1) {
            return new ReductionResult(subtaskResults.get(0).getVerdict(), (int) Math.round(subtaskResults.get(0).getScore()));
        } else {
            double score = 0;
            for (SubtaskResult result : subtaskResults) {
                score += result.getScore();
            }

            NormalVerdict worstVerdict = getWorstVerdict(Lists.transform(subtaskResults, r -> r.getVerdict()));

            if (worstVerdict == ScoringVerdict.ACCEPTED || worstVerdict == ScoringVerdict.OK) {
                return new ReductionResult(worstVerdict, (int) Math.round(score));
            } else {
                return new ReductionResult(ReductionVerdict.okWithWorstVerdict(worstVerdict), (int) Math.round(score));
            }
        }
    }

    protected final NormalVerdict getWorstVerdict(List<NormalVerdict> verdicts) {
        List<EvaluationVerdict> evaluationVerdicts = Lists.newArrayList();
        List<ScoringVerdict> scoringVerdicts = Lists.newArrayList();

        for (NormalVerdict verdict : verdicts) {
            if (verdict == EvaluationVerdict.SKIPPED) {
                continue;
            } else if (verdict instanceof EvaluationVerdict) {
                evaluationVerdicts.add((EvaluationVerdict) verdict);
            } else {
                scoringVerdicts.add((ScoringVerdict) verdict);
            }
        }

        Collections.sort(evaluationVerdicts);
        Collections.sort(scoringVerdicts);

        if (!evaluationVerdicts.isEmpty()) {
            return evaluationVerdicts.get(evaluationVerdicts.size() - 1);
        } else if (!scoringVerdicts.isEmpty()) {
            return scoringVerdicts.get(scoringVerdicts.size() - 1);
        } else {
            return ScoringVerdict.OK;
        }
    }

    protected final String improveScore(String score, String originalScore) {
        if (originalScore.isEmpty()) {
            return score;
        } else {
            return score + " (" + originalScore + ")";
        }
    }
}
