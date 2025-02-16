package com.mykare.user_management.webModel;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserWebModel {
	private Integer userId;
	private String emailId;
	private String password;
	private Boolean userIsActive;
	private Integer createdBy;
	private Date userCreatedOn;
	private Integer userUpdatedBy;
	private Date userUpdatedOn;
	private String userName;
	private String gender;
	private String userType;

}
