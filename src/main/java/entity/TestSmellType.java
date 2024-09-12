package entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestSmellType {
    Assertion_Roulette,
    Conditional_Test_Logic,
    Constructor_Initialization,
    Default_Test,
    EmptyTest,
    Exception_Catching_Throwing,
    General_Fixture,
    Mystery_Guest,
    Print_Statement,
    Redundant_Assertion,
    Sensitive_Equality,
    Verbose_Test,
    Sleepy_Test,
    Eager_Test,
    Lazy_Test,
    Duplicate_Assert,
    Unknown_Test,
    IgnoredTest,
    Resource_Optimism,
    Magic_Number_Test,
    Dependent_Test,
    ;
}