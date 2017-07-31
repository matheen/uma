package matheen.spring.aws.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.Setter;

@Entity(name="uma_user")
public class User{

	public User(){}
	
	public User(String firstName, String lastName, Date dateOfBirth, UserImage userImage, Address address) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		this.userImage = userImage;
		this.address = address;
	}

	@Id
	@Getter
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
	
	@Setter
	@Getter
	@Column(nullable = false, length = 30)
	private String firstName;
	
	@Setter
	@Getter
	@Column(nullable = false, length = 30)
	private String lastName;
	
	@Setter	
	@Getter
	@Column(nullable = false)
	private Date dateOfBirth;
	
	@Setter
	@Getter
	@OneToOne(cascade = {CascadeType.ALL})
	private UserImage userImage;
	
	@Setter
	@Getter
	@OneToOne(cascade = {CascadeType.ALL})
	private Address address;
}