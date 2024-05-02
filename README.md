# FoodIst Android App README

Welcome to [FoodIST](https://fenix.tecnico.ulisboa.pt/disciplinas/CMov/2018-2019/2-semestre), your go-to app for discovering delicious food options around you! 🍔🥗 Real-time information about where to eat at each campus in IST, what is on the menu, and the expected queue time.

## Overview
FoodIst is a mobile application developed as part of the *Mobile and Ubiquitous Computing* course during the 2nd semester of 2019/2020 semester. Our group, worked diligently to create a comprehensive solution for food enthusiasts. We nailed this project and scored a perfect grade of **20/20**! 🎉

## Team Members:
- **João Rafael Soares** (87675) - [JRafaelSoares](https://github.com/JRafaelSoares), (**Final Grade**: 18/20)
- **José Brás** (82069) @ [ist182069](https://github.com/ist182069), (**Final Grade**: 18/20)
- **Miguel Barros** (87691) - [MVBarros](https://github.com/MVBarros), (**Final Grade**: 18/20)

## Key Features
- **Listing Food Services:** Discover a variety of food services conveniently.
- **Dish Details:** Explore detailed information about dishes offered.
- **User Profiles:** Customize your experience with personalized user profiles.

## Technologies Used
- **GRPC:** Efficient client-server communication protocol.
- **TLS Authentication:** Ensuring secure communication between server and client.
- **Java 11:** Backend development using Gradle 6.3.
- **External Libraries:** Leveraged GoogleDirectionLibrary, Google translate, and Histogram for enhanced functionality.
- **Java 8:** Android app development with additional services like SimWifiP2pService for Termite simulation.

# Running the Server

To run the server, you must have:
- Java 11 
- Gradle

First, you go to the folder "FoodIST-Server" and run the command:

    gradle build

Then, you enter the folder "server" and run the script for Google Translation to work:

    source googleTranslate.sh
    
Finally, you run the server by running the command:

    gradle startServer

## Simplifications and Future Enhancements
- **Enhanced Authentication:** Proposing robust authentication mechanisms for improved security.
- **Database Integration:** Exploring integration with databases for better data management.
- **Cloud Deployment:** Considering deployment on cloud platforms for scalability.
- **Bug Fixes and Improvements:** Addressing language changes, IDE issues, and enhancing user experience.

## Conclusion
FoodIst represents our journey into mobile app development, highlighting challenges, learnings, and achievements. We express our gratitude to Professor Luís Pedrosa for his invaluable guidance and support.

Join us as we continue to innovate and create delightful experiences for food enthusiasts worldwide!

Cheers,  
José Brás
