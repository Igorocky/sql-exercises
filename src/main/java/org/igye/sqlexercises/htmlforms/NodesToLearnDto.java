package org.igye.sqlexercises.htmlforms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class NodesToLearnDto {
    private List<NodeDto> path;
    private List<NodeDto> nodesToLearn;
    private int transitionsTotalCnt;
    private int numberOfConnectedTransitions;
    private int numberOfCycles;

}
