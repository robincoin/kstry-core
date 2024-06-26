/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.test.diagram.config;

import cn.kstry.framework.core.bpmn.ServiceTask;
import cn.kstry.framework.core.bpmn.enums.IterateStrategyEnum;
import cn.kstry.framework.core.bpmn.extend.ElementIterable;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.joinpoint.InclusiveJoinPoint;
import cn.kstry.framework.core.component.bpmn.joinpoint.ParallelJoinPoint;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.bpmn.link.StartProcessLink;
import cn.kstry.framework.core.component.expression.Exp;
import cn.kstry.framework.test.diagram.constants.SCS;
import cn.kstry.framework.test.diagram.constants.StoryNameConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lykan
 */
@Configuration
public class Case1BpmnDiagramConfiguration {

    @Bean
    public SubProcessLink subProcessBuilder1() {
        return SubProcessLink.build("a-call-link-diagram-sub-process",
                link -> link
                        .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                        .nextSubProcess("req.d != null", Case1BpmnDiagramConfiguration::subProcessBuilder3).build()
                        .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                        .end()
        );
    }

    @Bean
    public SubProcessLink subProcessBuilder2() {
        return SubProcessLink.build("diagram-sub-process",
                link -> link
                        .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                        .nextSubProcess("Activity_0iw7vwf").build()
                        .nextSubProcess("a-call-link-diagram-sub-process").build()
                        .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                        .end()
        );
    }

    @Bean
    public SubProcessLink subProcessBuilder3() {
        return SubProcessLink.build(Case1BpmnDiagramConfiguration::subProcessBuilder3, link -> {
            ProcessLink eg1 = link
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextExclusive().build();

            eg1
                    .nextTask(Exp.b(e -> e.req("d").ge().value(0))).component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .end();
            eg1
                    .nextTask(Exp.b(e -> e.req("d").lt().value(0)), SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                    .end();
        });
    }

    @Bean
    public SubProcessLink subProcessBuilder4() {
        return SubProcessLink.build("ite-test-sub-process",
                link -> link.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ARRAY_ONE).property("test-prop").build().end());
    }

    @Bean
    public ProcessLink processLink1() {
        StartProcessLink processLink = StartProcessLink.build(StoryNameConstants.A001);
        ProcessLink buildOneChain = processLink.nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build();

        processLink.parallel().build().joinLinks().joinLinks(
                        processLink.inclusive().build().joinLinks(
                                buildOneChain
                                        .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                        .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build(),
                                buildOneChain
                                        .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                        .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                        .nextSubProcess("a-call-link-diagram-sub-process").build()
                        ),
                        processLink
                                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                )
                .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .nextSubProcess("diagram-sub-process").build()
                .nextTask().component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .end();
        return processLink;
    }

    @Bean
    public ProcessLink processLink2() {
        StartProcessLink processLink = StartProcessLink.build(StoryNameConstants.A002);

        ProcessLink exclusiveGateway = processLink.nextExclusive().build();
        exclusiveGateway
                .nextTask("req.a < 10", SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .end();

        ParallelJoinPoint par01 = exclusiveGateway.nextParallel("req.a >= 10", processLink.parallel().notStrictMode().openAsync().build());
        par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        par01.nextService(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink processLink3() {
        StartProcessLink processLink = StartProcessLink.build(StoryNameConstants.A003);

        ServiceTask st1 = ServiceTask.builder("s-02345-id1")
                .component(SCS.F.CALCULATE_SERVICE)
                .service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE)
                .ins();

        ProcessLink exclusiveGateway = processLink.nextExclusive().serviceTask(st1).build();
        exclusiveGateway
                .nextTask("req.a < 10",
                        ServiceTask.builder(null).component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).ins()
                )
                .nextTask(ServiceTask.builder(null).component(SCS.F.CALCULATE_SERVICE).service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE).ins())
                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build()
                .nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_TIMEOUT).timeout(1000).notStrictMode().build()
                .nextTask(SCS.F.CALCULATE_SERVICE, "aaa").allowAbsent().build()
                .end();

        ServiceTask st2 = ServiceTask.builder("s-02345-id2")
                .component(SCS.F.CALCULATE_SERVICE)
                .service(SCS.CALCULATE_SERVICE.F.INCREASE_ONE)
                .ins();
        InclusiveJoinPoint inc01 = exclusiveGateway.nextInclusive("req.a >= 10", processLink.inclusive().serviceTask(st2).openAsync().build());
        inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).name("XXX").build().end();
        inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        inc01.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ONE).build().end();
        return processLink;
    }

    @Bean
    public ProcessLink processLink4() {
        StartProcessLink processLink = StartProcessLink.build(StoryNameConstants.A004);
        processLink.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.INCREASE_ARRAY_ONE).property("test-prop")
                .iterable(ElementIterable.builder("sta.arr").openAsync().iteStrategy(IterateStrategyEnum.ALL_SUCCESS).build()).build()
                .end();
        return processLink;
    }

    @Bean
    public ProcessLink processLink5() {
        StartProcessLink processLink = StartProcessLink.build(StoryNameConstants.A005);
        processLink.nextSubProcess("ite-test-sub-process").timeout(0).notStrictMode()
                .iterable(ElementIterable.builder("sta.arr").openAsync().iteStrategy(IterateStrategyEnum.ALL_SUCCESS).build()).build()
                .end();
        return processLink;
    }

    @Bean
    public ProcessLink processLink6() {
        StartProcessLink processLink = StartProcessLink.build(StoryNameConstants.A006);
        processLink.nextSubProcess("ite-test-sub-process").timeout(1000)
                .iterable(ElementIterable.builder("sta.arr").openAsync().iteStrategy(IterateStrategyEnum.ALL_SUCCESS).build()).build()
                .end();
        return processLink;
    }

    @Bean
    public ProcessLink processLink7() {
        StartProcessLink processLink = StartProcessLink.build(StoryNameConstants.A007);
        processLink.nextTask(SCS.F.CALCULATE_SERVICE, SCS.CALCULATE_SERVICE.F.CALCULATE_ERROR).build().end();
        return processLink;
    }
}
