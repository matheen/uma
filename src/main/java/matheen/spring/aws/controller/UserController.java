package matheen.spring.aws.controller;

import matheen.spring.aws.exception.InvalidUserRequestException;
import matheen.spring.aws.exception.UserNotFoundException;
import matheen.spring.aws.model.Address;
import matheen.spring.aws.model.User;
import matheen.spring.aws.model.UserImage;
import matheen.spring.aws.repository.UserRepository;
import matheen.spring.aws.service.FileArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * User Controller exposes a series of RESTful endpoints
 */
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileArchiveService fileArchiveService;


    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public @ResponseBody
    User createUser(
            @RequestParam(value="firstName", required=true) String firstName,
            @RequestParam(value="lastName", required=true) String lastName,
            @RequestParam(value="dateOfBirth", required=true) @DateTimeFormat(pattern="yyyy-MM-dd") Date dateOfBirth,
            @RequestParam(value="street", required=true) String street,
            @RequestParam(value="town", required=true) String town,
            @RequestParam(value="county", required=true) String county,
            @RequestParam(value="postcode", required=true) String postcode,
            @RequestParam(value="image", required=true) MultipartFile image) {

        UserImage userImage = fileArchiveService.saveFileToS3(image);
        User user = new User(firstName, lastName, dateOfBirth, userImage,
                new Address(street, town, county, postcode));

        userRepository.save(user);
        return user;
    }

    /**
     * Get user using id. Returns HTTP 404 if user not found
     *
     * @param userId
     * @return retrieved user
     */
    @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
    public User getUser(@PathVariable("userId") Long userId) {
		
		/* validate user Id parameter */
        if (null==userId) {
            throw new InvalidUserRequestException();
        }

        User user = userRepository.findOne(userId);

        if(null==user){
            throw new UserNotFoundException();
        }

        return user;
    }

    /**
     * Gets all users.
     *
     * @return the users
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> getUsers() {

        return (List<User>) userRepository.findAll();
    }

    /**
     * Deletes the user with given user id if it exists and returns HTTP204.
     *
     * @param userId the user id
     */
    @RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
    public void removeUser(@PathVariable("userId") Long userId, HttpServletResponse httpResponse) {

        if(userRepository.exists(userId)){
            User user = userRepository.findOne(userId);
            fileArchiveService.deleteImageFromS3(user.getUserImage());
            userRepository.delete(user);
        }

        httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
    }

}