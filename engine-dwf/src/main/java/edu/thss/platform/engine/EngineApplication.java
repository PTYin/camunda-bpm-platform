package edu.thss.platform.engine;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@EnableProcessApplication
@SpringBootApplication
public class EngineApplication implements CommandLineRunner {
    @Autowired
    RepositoryService repositoryService;

    public static void main(String[] args) {
        SpringApplication.run(EngineApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().active().latestVersion().list();
        System.out.println("> 处于激活状态的最新版本的流程定义数量: " + processDefinitionList.size());
        for (ProcessDefinition pd : processDefinitionList) {
            System.out.println("\t ===> Process definition: " + pd.getKey() + " 版本：" + pd.getVersion());

        }

    }
}
