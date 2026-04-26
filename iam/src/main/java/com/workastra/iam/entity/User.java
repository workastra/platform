package com.workastra.iam.entity;

import com.ibm.icu.text.PersonName;
import com.ibm.icu.text.PersonNameFormatter;
import com.ibm.icu.text.PersonNameFormatter.Formality;
import com.ibm.icu.text.PersonNameFormatter.Length;
import com.ibm.icu.text.PersonNameFormatter.Usage;
import com.ibm.icu.text.SimplePersonName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table(name = "users")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class User implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column
    private UUID id;

    @Column
    private String username;

    @Getter(AccessLevel.NONE)
    @Column
    private @Nullable String password;

    @Column
    private @Nullable String familyName;

    @Column
    private @Nullable String middleName;

    @Column
    private String givenName;

    @Column
    private String gender;

    @Column
    private String email;

    @Column
    private boolean emailVerified;

    @Column
    private Instant emailVerifiedAt;

    @Column
    private Locale locale;

    @Getter(AccessLevel.NONE)
    @Column
    private boolean accountNonExpired;

    @Getter(AccessLevel.NONE)
    @Column
    private boolean accountNonLocked;

    @Getter(AccessLevel.NONE)
    @Column
    private boolean credentialsNonExpired;

    @Getter(AccessLevel.NONE)
    @Column
    private boolean enabled;

    @Column
    private ZoneId timezoneId;

    @Column(insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT })
    private Instant createdAt;

    @Column(insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT })
    private String createdBy;

    @Column(insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    private Instant updatedAt;

    @Column(insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    private String updatedBy;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public String getFullName() {
        SimplePersonName.Builder personNameBuilder = SimplePersonName.builder()
            .setLocale(this.locale)
            .addField(PersonName.NameField.GIVEN, null, this.givenName);

        if (this.familyName != null) {
            personNameBuilder.addField(PersonName.NameField.SURNAME, null, this.familyName);
        }

        if (this.middleName != null) {
            personNameBuilder.addField(PersonName.NameField.GIVEN2, null, this.middleName);
        }

        PersonNameFormatter formatter = PersonNameFormatter.builder()
            .setLocale(this.locale)
            .setLength(Length.LONG)
            .setUsage(Usage.REFERRING)
            .setFormality(Formality.FORMAL)
            .build();

        return formatter.formatToString(personNameBuilder.build());
    }
}
