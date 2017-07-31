package matheen.spring.aws.service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;

import matheen.spring.aws.exception.FileArchiveServiceException;
import matheen.spring.aws.model.UserImage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;


@Service
public class FileArchiveService {

	@Autowired
	private AmazonS3Client s3Client;

	private static final String S3_BUCKET_NAME = "uma-aws";


	/**
	 * Save image to S3 and return UserImage containing key and public URL
	 * 
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	public UserImage saveFileToS3(MultipartFile multipartFile) throws FileArchiveServiceException {

		try{
			File fileToUpload = convertFromMultiPart(multipartFile);
			String key = Instant.now().getEpochSecond() + "_" + fileToUpload.getName();

			/* save file */
			s3Client.putObject(new PutObjectRequest(S3_BUCKET_NAME, key, fileToUpload));

			/* get signed URL (valid for one year) */
			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(S3_BUCKET_NAME, key);
			generatePresignedUrlRequest.setMethod(HttpMethod.GET);
			generatePresignedUrlRequest.setExpiration(DateTime.now().plusYears(1).toDate());

			URL signedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest); 

			return new UserImage(key, signedUrl.toString());
		}
		catch(Exception ex){			
			throw new FileArchiveServiceException("An error occurred saving file to S3", ex);
		}		
	}

	/**
	 * Delete image from S3 using specified key
	 * 
	 * @param customerImage
	 */
	public void deleteImageFromS3(UserImage customerImage){
		s3Client.deleteObject(new DeleteObjectRequest(S3_BUCKET_NAME, customerImage.getKey()));	
	}

	/**
	 * Convert MultiPartFile to ordinary File
	 * 
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	private File convertFromMultiPart(MultipartFile multipartFile) throws IOException {

		File file = new File(multipartFile.getOriginalFilename());
		file.createNewFile(); 
		FileOutputStream fos = new FileOutputStream(file); 
		fos.write(multipartFile.getBytes());
		fos.close(); 

		return file;
	}
}