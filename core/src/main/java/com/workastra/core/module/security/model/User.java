package com.workastra.core.module.security.model;

import com.ibm.icu.text.PersonName;
import com.ibm.icu.text.PersonNameFormatter;
import com.ibm.icu.text.PersonNameFormatter.Formality;
import com.ibm.icu.text.PersonNameFormatter.Length;
import com.ibm.icu.text.PersonNameFormatter.Usage;
import com.ibm.icu.text.SimplePersonName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.hibernate.generator.EventType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table(name = "users")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column
    @UuidGenerator(style = Style.VERSION_7)
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
    private @Nullable String givenName;

    @Column
    @Builder.Default
    private String gender = "other";

    @Column
    private String email;

    @Column
    private boolean emailVerified;

    @Column
    private Instant emailVerifiedAt;

    @Column
    @Builder.Default
    private Locale locale = Locale.forLanguageTag("en-US");

    @Getter(AccessLevel.NONE)
    @Column
    @Builder.Default
    private boolean accountNonExpired = true;

    @Getter(AccessLevel.NONE)
    @Column
    @Builder.Default
    private boolean accountNonLocked = true;

    @Getter(AccessLevel.NONE)
    @Column
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Getter(AccessLevel.NONE)
    @Column
    @Builder.Default
    private boolean enabled = true;

    @Column
    @Builder.Default
    private ZoneId timezoneId = ZoneId.of("UTC");

    @Column(insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT })
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = true, updatable = false)
    @CreatedBy
    private User createdBy;

    @Column(insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", insertable = true, updatable = true)
    @LastModifiedBy
    private User updatedBy;

    @Column
    private @Nullable Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", insertable = true, updatable = true)
    private @Nullable User deletedBy;

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
        if (this.familyName == null && this.middleName == null && this.givenName == null) {
            return this.getUsername();
        }

        SimplePersonName.Builder personNameBuilder = SimplePersonName.builder().setLocale(this.locale);

        if (this.familyName != null) {
            personNameBuilder.addField(PersonName.NameField.SURNAME, null, this.familyName);
        }

        if (this.middleName != null) {
            personNameBuilder.addField(PersonName.NameField.GIVEN2, null, this.middleName);
        }

        if (this.givenName != null) {
            personNameBuilder.addField(PersonName.NameField.GIVEN, null, this.givenName);
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
