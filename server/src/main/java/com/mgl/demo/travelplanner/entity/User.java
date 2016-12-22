package com.mgl.demo.travelplanner.entity;

import com.mgl.demo.travelplanner.entity.support.BaseEntity;

import static org.hibernate.id.enhanced.SequenceStyleGenerator.INCREMENT_PARAM;
import static org.hibernate.id.enhanced.SequenceStyleGenerator.SEQUENCE_PARAM;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(
        indexes = {
            @Index(name = "user__first_name_idx", columnList = "firstName"),
            @Index(name = "user__last_name_idx", columnList = "lastName")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "user__email_uidx", columnNames = {"email"})
        }
) 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@ToString(callSuper = true, exclude = {"trips"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class User extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    private static final String NO_LAST_NAME = "";

    private static final int EMAIL_MIN_LEN = 3;
    private static final int EMAIL_MAX_LEN = 64;

    private static final String VALID_EMAIL_REGEX = ".+@.+";
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile(VALID_EMAIL_REGEX);

    private static final int PASSWORD_SALT_GEN_ROUNDS = 10;

    // Sample BCrypt hasssed password: $2a$10$MN4pQWWQAzDWVcme7gq/L.oUEQ42bPRanRkupHDEmxLRUQv7dmM/e
    private static final int PASSWORD_MIN_LEN = 60;
    private static final int PASSWORD_MAX_LEN = 60;

    private static final String PLAIN_VALID_PASSWORD_REGEX = "^[0-9a-zA-Z\\_\\-]+$";
    private static final Pattern PLAIN_VALID_PASSWORD_PATTERN = Pattern.compile(PLAIN_VALID_PASSWORD_REGEX);

    private static final int FIRST_NAME_MIN_LEN = 1;
    private static final int FIRST_NAME_MAX_LEN = 64;

    private static final int LAST_NAME_MIN_LEN = 0;
    private static final int LAST_NAME_MAX_LEN = 128;

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_gen")
    @GenericGenerator(name = "user_id_gen", strategy = ENHANCED_SEQ,
            parameters = {
                @Parameter(name = SEQUENCE_PARAM, value = "tp_user_id_seq"),
                @Parameter(name = INCREMENT_PARAM, value = ENHANCED_SEQ_INCREMENT),
            })
    @ColumnDefault("nextval('tp_user_id_seq')")
    private Long id;

    @NotNull
    @NotBlank
    @Email(regexp = VALID_EMAIL_REGEX)
    @Size(min = EMAIL_MIN_LEN, max = EMAIL_MAX_LEN)
    @Column(nullable = false, length = EMAIL_MAX_LEN)
    private String email;

    @NotNull
    @NotBlank
    @Size(min = PASSWORD_MIN_LEN, max = PASSWORD_MAX_LEN)
    @Column(nullable = false, length = PASSWORD_MAX_LEN)
    private String password;

    @NotNull
    @NotBlank
    @Size(min = FIRST_NAME_MIN_LEN, max = FIRST_NAME_MAX_LEN)
    @Column(nullable = false, length = FIRST_NAME_MAX_LEN)
    private String firstName;

    @NotNull
    @Size(min = LAST_NAME_MIN_LEN, max = LAST_NAME_MAX_LEN)
    @Column(nullable = false, length = LAST_NAME_MAX_LEN)
    @ColumnDefault("''")
    private String lastName = NO_LAST_NAME;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = {CascadeType.REMOVE})
    private Set<Trip> trips;

    public User(String email, String firstName, String lastName) {
        this.email = Objects.requireNonNull(email, "email");
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
    }

    public User(String email, String firstName) {
        this(email, firstName, NO_LAST_NAME);
    }

    public static boolean isValidEmail(String email) {
        return VALID_EMAIL_PATTERN.matcher(Strings.nullToEmpty(email)).matches();
    }

    public static boolean isValidPlainPassword(String plainPassword) {
        return PLAIN_VALID_PASSWORD_PATTERN.matcher(Strings.nullToEmpty(plainPassword)).matches();
    }

    public static String encryptPlainPassword(String plainPassword) {
        String salt = BCrypt.gensalt(PASSWORD_SALT_GEN_ROUNDS);
        return BCrypt.hashpw(plainPassword, salt);
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(getFirstName());
        if (!getLastName().isEmpty()) {
            sb.append(getLastName());
        }
        return sb.toString();
    }

}