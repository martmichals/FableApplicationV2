package com.example.fableapplicationv2;

import android.content.Context;
import android.util.Log;

//Class to check user inputs, formatting
public class DataVerification {
    public static int MIN_PASSWORD_CHARS = 8;
    public static final int MAX_PASSWORD_CHARS = 20;
    public static final int PASSWORD_UPPERCASE_CHARS_REQUIRED = 1;
    public static final int PASSWORD_LOWERCASE_CHARS_REQUIRED = 1;
    public static final int PASSWORD_NUMBERS_REQUIRED = 1;
    public static final int ZIP_CODE_LENGTH = 5;
    public static final int PHONE_NUMBER_LENGTH = 11;
    public static final int SLOGAN_WORD_LIMIT = 5;

    public static final String TAG = "DataVerification";
    public static final String EMAIL_SYMBOL = "@";
    public static final String DOT = ".";
    public static final char SPACE = ' ';

    /** Method to check email validity
     * @return null   -> email is valid
     *         string -> error message for the user
     * Important to note that method does not check if the email is a real email
     * @param email : email to check
     */
    public static String checkEmail(Context context, String email){
        if(email.length() == 0)
            return context.getString(R.string.requiredEntry);
        if(!email.contains(EMAIL_SYMBOL))
            return context.getString(R.string.emailNotValid);
        if(!email.contains(DOT))
            return context.getString(R.string.emailNotValid);
        return null;
    }

    /** Method to check password validity
     * @return null   -> password is valid
     *         string -> error message for the user
     * @contains : 8 characters in length
     *              Less than 26 chars in length
     *              Contains uppercase and lowercase characters
     *              Contains numbers
     * @param context : context of the activity calling the method
     * @param password : password entered into the first form
     * @param passwordConfirmation: password entered into the password confirmation field
     */
    public static String checkPassword(Context context, String password, String passwordConfirmation){
        if(!password.equals(passwordConfirmation)){
            Log.d(TAG, "Passwords entered into the forms do not match");
            return context.getString(R.string.passwordNoMatch);
        }

        String errorToReturn = "";
        int passwordLength = password.length();
        if(passwordLength < MIN_PASSWORD_CHARS){
            Log.d(TAG, "Password does not have enough characters");
            errorToReturn+= context.getString(R.string.passwordTooShort) + "\n";
        }else if(passwordLength > MAX_PASSWORD_CHARS){
            Log.d(TAG, "Password has too many characters");
            errorToReturn+= context.getString(R.string.passwordTooLong) + "\n";
        }

        if(!checkForLowercaseLetters(password, PASSWORD_LOWERCASE_CHARS_REQUIRED))
            errorToReturn+= context.getString(R.string.passwordNoLowercase) + "\n";

        if(!checkForUppercaseLetters(password, PASSWORD_UPPERCASE_CHARS_REQUIRED))
            errorToReturn+= context.getString(R.string.passwordNoCapitals) + "\n";

        if(!checkForNumbers(password, PASSWORD_NUMBERS_REQUIRED))
            errorToReturn+= context.getString(R.string.passwordNoNumbers);

        if(errorToReturn.length() > 0)
            return errorToReturn;

        return null;
    }

    /** Method to check name validity
     * @param context : context of the activity
     * @param name : name entered into the form
     * @return error message if bad name, null if no error message
     */
    public static String checkName(Context context, String name){
        if(name.matches(".*\\d+.*"))
            return context.getString(R.string.nameNoNumbers);
        else if(name.length() == 0)
            return context.getString(R.string.requiredEntry);
        else
            return null;
    }

    /** Quick method to check street address validity
     * @param context : context of the activity
     * @param streetAddress : address the user entered into field
     * @return error message if field left blank, null otherwise
     */
    public static String checkStreetAddress(Context context, String streetAddress){
        if(streetAddress.length() == 0)
            return context.getString(R.string.requiredEntry);
        else
            return null;
    }

    /** Quick method to check city validity
     * @param context : context of the activity
     * @param city : city the user entered into field
     * @return error message if field left blank, null otherwise
     */
    public static String checkCityValidity(Context context, String city){
        if(city.length() == 0)
            return context.getString(R.string.requiredEntry);
        else
            return null;
    }

    /** Method to check if zip code is valid
     * @param context : context of the activity
     * @param zipCode : user entered string, to check for only numbers
     * @return string error message, null if the string is all digits + 5 digits long
     */
    public static String checkZipValidity(Context context, String zipCode){
        if(zipCode.length() == 0)
            return context.getString(R.string.requiredEntry);
        else if(zipCode.length() != ZIP_CODE_LENGTH)
            return context.getString(R.string.entryIncorrectLength);
        for(int i = 0; i < zipCode.length(); i++){
            if(!Character.isDigit(zipCode.charAt(i)))
                return context.getString(R.string.onlyNumbers);
        }
        return null;
    }

    /** Method to check if zip code is valid
     * @param context : context of the activity
     * @param phoneNumber : user entered string, to check for only numbers
     * @return string error message, null if the string is all digits + 10 digits long
     */
    public static String checkPhoneNumberValidity(Context context, String phoneNumber){
        if(phoneNumber.length() == 0)
            return context.getString(R.string.requiredEntry);
        else if(phoneNumber.length() != PHONE_NUMBER_LENGTH)
            return context.getString(R.string.phoneNumberIncorrectFormat);
        for(int i = 0; i < phoneNumber.length(); i++){
            if(!Character.isDigit(phoneNumber.charAt(i)))
                return context.getString(R.string.onlyNumbers);
        }
        return null;
    }

    /**Method to see if the desired amount of lower case characters are in a string
     * @param str : string to check for the lower case characters
     * @param numLowercaseDesired : number of lower case letters to check for in the word
     * @return true if that many chars found, false if not enough lowercase letters found
     */
    public static boolean checkForLowercaseLetters(String str, int numLowercaseDesired){
        int numLowercaseFound = 0;
        for(int i = 0; i < str.length(); i++){
            if(Character.isLowerCase(str.charAt(i)))
                numLowercaseFound++;
        }
        Log.d(TAG, "Found " + numLowercaseFound + " lowercase chars in password entered");

        if(numLowercaseFound < numLowercaseDesired)
            return false;
        else
            return true;
    }

    /**Method to see if the desired amount of upper case characters are in a string
     * @param str : string to check for upper case chars
     * @param numUppercaseDesired : number of uppercase chars to check for in the string
     * @return : true if desired amount of upper case chars in string, false if not
     */
    public static boolean checkForUppercaseLetters(String str, int numUppercaseDesired){
        int numUppercaseFound = 0;
        for(int i = 0; i < str.length(); i++){
            if(Character.isUpperCase(str.charAt(i)))
                numUppercaseFound++;
        }
        Log.d(TAG, "Found " + numUppercaseFound + " uppercase chars in password entered");

        if(numUppercaseFound < numUppercaseDesired)
            return false;
        else
            return true;
    }

    /** Method to check if the passed string has the number of digits required
     * @param str : string to check for numbers
     * @param numNumbersDesired : numbers desired in the passed string
     * @return : true if num numbers found in the string / false if not
     */
    public static boolean checkForNumbers(String str, int numNumbersDesired){
        int numNumbersFound = 0;
        for(int i = 0; i < str.length(); i++){
            if(Character.isDigit(str.charAt(i)))
                numNumbersFound++;
        }
        Log.d(TAG, "Found " + numNumbersFound + " number chars in password entered");

        if(numNumbersFound < numNumbersDesired)
            return false;
        else
            return true;
    }

    /**Method to check the slogan users are asked to enter for their seller profile
     * @param slogan : the string slogan the user entered
     * @return true if the slogan is 5 words or less, false if more
     */
    public static boolean checkSellerSlogan(String slogan){
        if(slogan.length() == 0)
            return true;

        int numSpaces = 0;
        for(int i = 0; i < slogan.length(); i++){
            if(slogan.charAt(i) == SPACE && (i != slogan.length() - 1)){
                numSpaces++;
            }
        }

        if(numSpaces < SLOGAN_WORD_LIMIT)
            return true;

        return false;
    }
}
