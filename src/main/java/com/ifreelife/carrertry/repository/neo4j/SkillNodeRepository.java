package com.ifreelife.carrertry.repository.neo4j;

import com.ifreelife.carrertry.entity.neo4j.SkillNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SkillNodeRepository extends Neo4jRepository<SkillNode, String> {
}
