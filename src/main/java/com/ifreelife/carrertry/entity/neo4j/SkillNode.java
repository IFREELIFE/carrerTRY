package com.ifreelife.carrertry.entity.neo4j;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Getter
@Setter
@Node("Skill")
public class SkillNode {

    @Id
    private String name;

    public SkillNode(String name) {
        this.name = name;
    }
}
