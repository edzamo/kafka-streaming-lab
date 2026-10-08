package com.example.banking.interbank.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/** Reglas hexagonales: INV-01, INV-02 (con EXC-1), INV-03, INV-10, INV-12. */
@AnalyzeClasses(packages = "com.example.banking.interbank",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BASE = "com.example.banking.interbank";

    // INV-01: el dominio no depende de nada fuera de sí mismo (ni Reactor: EXC-1 solo cubre application)
    @ArchTest
    static final ArchRule domainIsPure = noClasses().that().resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    BASE + ".application..", BASE + ".adapter..", BASE + ".config..",
                    "org.springframework..", "reactor..", "com.fasterxml..", "jakarta..",
                    "org.slf4j..", "lombok..", "org.apache.kafka..")
            .allowEmptyShould(true);

    // INV-02 / INV-12: application sin adaptadores ni framework (solo @Service) ni Lombok/slf4j/Jackson
    @ArchTest
    static final ArchRule applicationHasNoFramework = noClasses().that().resideInAPackage(BASE + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    BASE + ".adapter..", BASE + ".config..",
                    "org.springframework.web..", "org.springframework.data..", "org.springframework.cloud..",
                    "org.springframework.messaging..", "org.springframework.beans..", "org.springframework.boot..",
                    "com.fasterxml..", "jakarta..", "org.slf4j..", "lombok..", "org.apache.kafka..")
            .allowEmptyShould(true);

    // INV-03: los adaptadores no se referencian entre sí
    @ArchTest
    static final ArchRule inboundDoesNotUseOutbound = noClasses().that().resideInAPackage(BASE + ".adapter.in..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.out..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule outboundDoesNotUseInbound = noClasses().that().resideInAPackage(BASE + ".adapter.out..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.in..")
            .allowEmptyShould(true);

    // Los clientes generados quedan confinados a adapter.out.http
    @ArchTest
    static final ArchRule generatedClientsConfined = noClasses()
            .that().resideOutsideOfPackage(BASE + ".adapter.out.http..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.out.http.generated..")
            .allowEmptyShould(true);

    // Nadie depende de la configuración salvo el arranque; el core no la ve (cableado solo en el borde)
    @ArchTest
    static final ArchRule coreDoesNotSeeConfig = noClasses()
            .that().resideInAnyPackage(BASE + ".domain..", BASE + ".application..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".config..")
            .allowEmptyShould(true);

    // INV-10: sin ciclos entre paquetes de primer nivel
    @ArchTest
    static final ArchRule noCycles = slices().matching(BASE + ".(*)..")
            .should().beFreeOfCycles().allowEmptyShould(true);
}
