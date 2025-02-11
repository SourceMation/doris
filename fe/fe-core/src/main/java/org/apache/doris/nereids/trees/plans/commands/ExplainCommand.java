// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.trees.plans.commands;

import org.apache.doris.analysis.ExplainOptions;
import org.apache.doris.common.AnalysisException;
import org.apache.doris.nereids.NereidsPlanner;
import org.apache.doris.nereids.glue.LogicalPlanAdapter;
import org.apache.doris.nereids.rules.exploration.mv.InitMaterializationContextHook;
import org.apache.doris.nereids.trees.plans.Explainable;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.logical.LogicalPlan;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;
import org.apache.doris.qe.ConnectContext;
import org.apache.doris.qe.StmtExecutor;

/**
 * explain command.
 */
public class ExplainCommand extends Command implements NoForward {

    /**
     * explain level.
     */
    public enum ExplainLevel {
        NONE(false),
        NORMAL(false),
        VERBOSE(false),
        TREE(false),
        GRAPH(false),
        PARSED_PLAN(true),
        ANALYZED_PLAN(true),
        REWRITTEN_PLAN(true),
        OPTIMIZED_PLAN(true),
        SHAPE_PLAN(true),
        MEMO_PLAN(true),
        DISTRIBUTED_PLAN(true),
        ALL_PLAN(true)
        ;

        public final boolean isPlanLevel;

        ExplainLevel(boolean isPlanLevel) {
            this.isPlanLevel = isPlanLevel;
        }
    }

    private final ExplainLevel level;
    private final LogicalPlan logicalPlan;
    private final boolean showPlanProcess;

    public ExplainCommand(ExplainLevel level, LogicalPlan logicalPlan, boolean showPlanProcess) {
        super(PlanType.EXPLAIN_COMMAND);
        this.level = level;
        this.logicalPlan = logicalPlan;
        this.showPlanProcess = showPlanProcess;
    }

    @Override
    public void run(ConnectContext ctx, StmtExecutor executor) throws Exception {
        LogicalPlan explainPlan;
        if (!(logicalPlan instanceof Explainable)) {
            throw new AnalysisException(logicalPlan.getClass().getSimpleName() + " cannot be explained");
        }
        explainPlan = ((LogicalPlan) ((Explainable) logicalPlan).getExplainPlan(ctx));
        LogicalPlanAdapter logicalPlanAdapter = new LogicalPlanAdapter(explainPlan, ctx.getStatementContext());
        ExplainOptions explainOptions = new ExplainOptions(level, showPlanProcess);
        logicalPlanAdapter.setIsExplain(explainOptions);
        executor.setParsedStmt(logicalPlanAdapter);
        NereidsPlanner planner = new NereidsPlanner(ctx.getStatementContext());
        if (ctx.getSessionVariable().isEnableMaterializedViewRewrite()) {
            ctx.getStatementContext().addPlannerHook(InitMaterializationContextHook.INSTANCE);
        }
        planner.plan(logicalPlanAdapter, ctx.getSessionVariable().toThrift());
        executor.setPlanner(planner);
        executor.checkBlockRules();
        if (showPlanProcess) {
            executor.handleExplainPlanProcessStmt(planner.getCascadesContext().getPlanProcesses());
        } else {
            executor.handleExplainStmt(planner.getExplainString(explainOptions), true);
        }
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitExplainCommand(this, context);
    }

    public ExplainLevel getLevel() {
        return level;
    }

    public LogicalPlan getLogicalPlan() {
        return logicalPlan;
    }

    public boolean showPlanProcess() {
        return showPlanProcess;
    }
}
