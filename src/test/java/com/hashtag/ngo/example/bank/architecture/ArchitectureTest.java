package com.hashtag.ngo.example.bank.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Règles d'architecture vérifiées automatiquement à chaque exécution des
 * tests (archunit-junit5) : elles échouent si le code s'écarte du découpage
 * en couches ou des conventions de nommage établies pour ce projet. Analyse
 * uniquement les classes de production (src/main), pas les classes de test.
 */
@AnalyzeClasses(packages = "com.hashtag.ngo.example.bank", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    /**
     * api -> bean -> entity, sans remontée : chaque couche ne peut être
     * utilisée que par celle(s) qui lui sont immédiatement supérieures.
     */
    @ArchTest
    static final ArchRule lesCouchesSontRespectees = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Api").definedBy("..api..")
            .layer("Bean").definedBy("..bean..")
            .layer("Entity").definedBy("..entity..")
            .whereLayer("Api").mayNotBeAccessedByAnyLayer()
            .whereLayer("Bean").mayOnlyBeAccessedByLayers("Api")
            .whereLayer("Entity").mayOnlyBeAccessedByLayers("Bean", "Api");

    @ArchTest
    static final ArchRule lesControleursSontNommesController = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller");

    /** Les interfaces de la couche bean (hors bean.impl) sont les contrats de service. */
    @ArchTest
    static final ArchRule lesInterfacesDeServiceSontNommeesService = classes()
            .that().resideInAPackage("..bean").and().areInterfaces()
            .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule lesImplementationsSontNommeesServiceImpl = classes()
            // areTopLevelClasses() exclut les classes synthétiques générées
            // par le compilateur pour les lambdas/expressions switch (ex.
            // TransactionServiceImpl$1), qui n'ont pas de nom "métier".
            .that().resideInAPackage("..bean.impl").and().areTopLevelClasses()
            .should().haveSimpleNameEndingWith("ServiceImpl");

    /**
     * Toute classe *Impl doit vivre dans un package se terminant par .impl...
     * à l'exception des *MapperImpl générés par MapStruct (ex.
     * AccountMapperImpl), qui suivent leur propre convention (à côté de
     * l'interface qu'ils implémentent) et ne sont pas concernés par cette
     * règle, pensée pour le code écrit à la main. Le marqueur
     * {@code @Generated} de MapStruct a une {@code @Retention(SOURCE)} : il
     * n'existe plus dans le bytecode et ArchUnit ne peut donc pas s'y fier
     * pour filtrer ; on exclut ces classes par leur nom à la place.
     */
    @ArchTest
    static final ArchRule lesClassesImplResidentDansUnPackageImpl = classes()
            .that().haveSimpleNameEndingWith("Impl").and().haveSimpleNameNotEndingWith("MapperImpl")
            .should().resideInAPackage("..impl");

    /** ...et symétriquement, les interfaces ne doivent jamais s'y trouver. */
    @ArchTest
    static final ArchRule lesInterfacesNeResidentPasDansUnPackageImpl = classes()
            .that().areInterfaces()
            .should().resideOutsideOfPackage("..impl..");
}
