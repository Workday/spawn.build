package build.spawn.application.facet;

import build.base.foundation.iterator.matching.IteratorPatternMatchers;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.injection.InjectionFramework;
import build.codemodel.jdk.JDKCodeModel;
import build.spawn.application.facet.facets.Address;
import build.spawn.application.facet.facets.Hobbies;
import build.spawn.application.facet.facets.InternationalAddress;
import build.spawn.application.facet.facets.Name;
import build.spawn.application.facet.facets.Occupation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Faceted} instances.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
class FacetedTests {

    /**
     * Creates a new {@link JDKCodeModel}.
     *
     * @return a new {@link JDKCodeModel}
     */
    private JDKCodeModel createCodeModel() {
        final var nameProvider = new NonCachingNameProvider();
        return new JDKCodeModel(nameProvider);
    }

    /**
     * Create a new {@link InjectionFramework} for creating {@link Faceted} {@link Object}s.
     *
     * @return a new {@link InjectionFramework}
     */
    private InjectionFramework createInjectionFramework() {
        final var codemodel = createCodeModel();
        return new InjectionFramework(codemodel);
    }

    /**
     * Ensures that a Faceted proxy instance can be created from a set of Facets and that it can be cast to its
     * comprising Facets properly through casting or through its as method.
     */
    @Test
    void shouldCreateFacetedPerson() {

        final var context = createInjectionFramework().newContext();

        final Faceted person = Faceted.create(context,
            Facet.of(Address.class, Address.Implementation.class),
            Facet.of(Hobbies.class, Hobbies.Implementation.class),
            Facet.of(Name.class, Name.Implementation.class),
            Facet.of(Occupation.class, Occupation.Implementation.class));

        final var interfaceMatcher = IteratorPatternMatchers.<Class<?>>starts()
            .thenLater().matches(Faceted.class)
            .then().matches(Address.class)
            .then().matches(Hobbies.class)
            .then().matches(Name.class)
            .then().matches(Occupation.class);

        assertThat(interfaceMatcher.test(Stream.of(person.getClass().getInterfaces())))
            .isTrue();

        final var address = person.as(Address.class);
        assertThat(address)
            .isPresent();

        assertThat(address
            .map(Address::getCity))
            .contains("Pleasanton");

        final var hobbies = (Hobbies) person;
        assertThat(hobbies.getHobbies()
            .map(Hobbies.Hobby::getHobby))
            .anyMatch(hobby -> hobby.equals("hiking"));

        final var name = person.as(Name.class);
        assertThat(name)
            .isPresent();

        assertThat(name
            .map(Name::getFirstName))
            .contains("Alyssa");

        assertThat(name
            .map(Name::getLastName))
            .contains("Hacker");

        final var occupation = (Occupation) person;
        assertThat(occupation.getOccupation())
            .isEqualTo("Software Development Engineer");
    }

    /**
     * Ensures that the {@link Optional} returned by a call to {@link Faceted#as} is not present when the {@link Facet}
     * requested is not included in the {@link Faceted} instance.
     */
    @Test
    void shouldNotGetAbsentFacet() {

        final var context = createInjectionFramework().newContext();

        final Faceted faceted = Faceted.create(context,
            Facet.of(Address.class, Address.Implementation.class));

        final var absentName = faceted.as(Name.class);

        assertThat(absentName)
            .isNotPresent();
    }

    /**
     * Ensures that a {@link Faceted} instance cannot be created with two backing implementations for a single
     * interface.
     */
    @Test
    void shouldNotAllowMultipleImplementationsInFaceted() {

        final var context = createInjectionFramework().newContext();

        assertThrows(IllegalArgumentException.class,
            () -> Faceted.create(context,
                Facet.of(Address.class, Address.Implementation.class),
                Facet.of(Address.class, Address.Implementation.class)));
    }

    /**
     * Ensures that a {@link Faceted} instance may be cast to a subclass and still invoke methods from a superclass.
     */
    @Test
    void shouldBeAbleToCastToExtendedAddressAndUseAddressMethods() {

        final var context = createInjectionFramework().newContext();

        final var faceted = Faceted.create(context,
            Facet.of(InternationalAddress.class, InternationalAddress.Implementation.class),
            Facet.of(Name.class, Name.Implementation.class));

        final var address = (InternationalAddress) faceted;

        assertThat(address.getCountry())
            .isEqualTo("US");

        assertThat(address.getStreet())
            .isEqualTo("Stoneridge Mall Road");
    }

    /**
     * Ensures that a {@link Faceted} instance can find an implementation for a requested interface even when there is
     * only a subclass implementing that interface.
     */
    @Test
    void shouldAcquireSubclassImplementationForSuperclassInterface() {

        final var context = createInjectionFramework().newContext();

        final var faceted = Faceted.create(context,
            Facet.of(InternationalAddress.class, InternationalAddress.Implementation.class));

        final var address = faceted.as(Address.class);
        assertThat(address)
            .isPresent();

        assertThat(address
            .map(Address::getZipCode))
            .contains(94588);

        final var internationalAddress = faceted.as(InternationalAddress.class);

        assertThat(internationalAddress)
            .isPresent();

        assertThat(internationalAddress
            .map(InternationalAddress::getCountry))
            .contains("US");

        final var interfaceMatcher = IteratorPatternMatchers
            .<Class<?>>starts().then()
            .matches(Faceted.class).then()
            .matches(InternationalAddress.class);

        assertThat(interfaceMatcher.test(Stream.of(faceted.getClass().getInterfaces())))
            .isTrue();
    }

    /**
     * Ensures that a {@link Faceted} object can be cast from {@link Facet} to {@link Facet}, including interfaces which
     * are superclasses of a present {@link Facet} interface.
     */
    @Test
    void shouldCastFromFacetToFacet() {

        final var context = createInjectionFramework().newContext();

        final var faceted = Faceted.create(context,
            Facet.of(InternationalAddress.class, InternationalAddress.Implementation.class),
            Facet.of(Name.class, Name.Implementation.class));

        final var address = (Address) faceted;
        assertThat(address.getNumber())
            .isEqualTo(6140);

        final var name = (Name) address;
        assertThat(name.getFirstName())
            .isEqualTo("Alyssa");
    }

    /**
     * Ensures that Faceted instances have referential equality.
     */
    @Test
    void shouldHaveReferentialEqualityForFaceted() {

        final var context = createInjectionFramework().newContext();

        final var faceted = Faceted.create(context,
            Facet.of(Address.class, Address.Implementation.class),
            Facet.of(Name.class, Name.Implementation.class));

        final var address = (Address) faceted;
        final var name = faceted.as(Name.class)
            .orElseThrow();

        assertThat(address)
            .isEqualTo(address);

        assertThat(address)
            .isEqualTo(name);

        assertThat(address.hashCode())
            .isEqualTo(address.hashCode());
    }
}
