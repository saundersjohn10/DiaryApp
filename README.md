# DiaryApp

App Utlizes:
- Java
- Firebase
- Firebase Authentication
- Account creation
- Compareable based on time

The overall app has a basic design and layout.  Firebase was the technology I wanted to explore and the simplest way I thought of saving data and grabbing data was through a Diary app.  The app has a theme of being a diary app but could also be used as a notes app.  I wanted the saving of the data to have a "Google Docs" feel to it where the data or words typed into the app are automatically uploaded to the cloud and you can watch them being updated.  

Using Firebase and its features-
Firebase and Andriod studio make it simple and free to connect an app you are creating to its servers which made using its software the ideal choice.  I wanted to make the app have an account feature where the user inputted information about themselves and created an account that allowed them access to the diary entries from any phone.  Firebase had a feature already ready for that something like that.  They also made the connection between the data on your servers and the user very secure using a unique identifier that is associated with a user when they make an account. This identifier was the branch to all the json data from that user and made it simple to parse through the data from a specific user.  I also utlized the reset of a password feature from Firebase.  If the user forgets their password, they click the link at the login screen and an email will be sent to them allowing them to reset their password.

The Note class-
A note class was created that stores a single entry.  The class implements the Compareable class in terms of the last time it was updated and this allows the notes to be sorted when they are laid out for the user to choose from.  Only primative types are allowed to be stored on the Firebase server which means that the values from the note class are saved and not the actual objects. When the program starts, all the notes are recreated and stored in a TreeSet (allowing for the ordering to be kept).  

