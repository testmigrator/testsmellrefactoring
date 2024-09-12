package entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestRefactorRule {
    REMOVE,
    LLM,
    FILE
}
