# COMP329 Robotics and Autonomous Systems Assignment 2

### Ashley Rushworth, James Daniels and Octavia Costache

This assessment covers the design of a hybrid robot which utilises AgentSpeak to determine what Intentions to execute, based on a set of plans, and uses these to direct a reactive (behaviour-based) robot working on the EV3 robot. The agent will be part of a multi-agent system for a search and rescue mission.

**This assignment counts for 50% of the final grade for the module. The deadline for the assignment  is Friday 6th December (i.e. end of week 11).  You will also be expected to give a demo of your solution, and answer questions during an allocated slot for 25 minutes during week 12.**

**The syllabus provides an indication of how much time a typical student is expected to spend on the assignment.  Please try to plan your activities to stay in line with this.  Whilst it is understood that you may require further time to complete part of your work, or that you may choose to spend more time to achieve a certain functionality (for example if you choose to challenge yourself using the sensor model), it is not expected that you should take significantly longer than this time.**

The scenario is as follows: a **doctor agent** (located at the hospital) has information about **5 possible locations of 3 victims** in a given area, and the location of obstacles. A paramedic agent chooses to participate in the search and rescue mission (in response to a CNP call for proposals), and receives the brief (including details of the possible location of obstacles and victims). It is believed that some of the victims are non-urgent, but others are urgent - only the doctor can determine this. The paramedic agent should try to locate the three victims in the smallest number of moves possible, and take them to the hospital; however, it can only take one at a time. It should prioritise finding the critical victims first; it should not move any of the non-urgent victims until the urgent victims have been taken to the hospital. The mission is complete when all three victims have been taken to the hospital.

For this assignment, you can assume that the starting point of the paramedic agent is the hospital.  For those groups that want to be challenged, then up to 6 marks can be obtained by assuming that the starting position of the paramedic agent is unknown.  In which case, they should determine this starting position using a particle filter.

### Doctor Agent

The doctor agent is responsible for requesting the assistance of the paramedic agent (using the Contract Net Protocol) and for providing details of the location of obstacles and possible victims.  It is also responsible for determining if a located victim is critical or non critical.

- The doctor agent will provide exact details on how many critical victims there are, and how many non-critical victims there are.
- It will also include details on how to determine victims critical status.

### Paramedic Agent

- The paramedic agent should aim to complete its mission in less than 10 minutes.  At the end of its mission it should return to the hospital.
- The paramedic agent should use the contract net protocol to respond to a request for assistance from the doctor agent.
- When a paramedic agent finds a victim, it should check with the doctor agent to check if the victim is critical or not.  The robot should also indicate (using an audio and/or visual cue such as changing the colour of the buttons) to indicate when a victim is found.
- **The paramedic can only take a single victim to the hospital.** Therefore:
  - If a critical victim is found then the paramedic agent should take the victim immediately to the hospital, before resuming its search.
  - If a non-critical victim is found, then the paramedic agents behaviour should be as follows:
    - if all the critical victims have already been found, then the non-critical victim should be taken to the hospital before resuming its search;
    - if critical victims are still to be found then the location of the non-critical victim should be stored, and the search continued.
  - If the location of the non-critical victims are known; once the paramedic agent has taken all of the critical victims to the hospital, it should return to the non-critical victims and escort them to the hospital.
- Not all of the victim locations need to be visited; once all three victims have been found, there will be no need to check the other locations.

- The paramedic agent should provide in real time on the PC screen the status of the rescue mission.  This should include:
- details of which of the 5 victim locations has been checked;
- the location of any victims found (and their severity);
- and the location (or possible locations) of the robot.
- The real-time details can be presented textually, but extra marks will be awarded for presenting this information graphically
- Note - there is no requirement to show details on the EV3 LCD display.  You can use this as you want.

- The Paramedic Agent should consist of **three main components**, that satisfy the following requirements:
  - The **AgentSpeak agent** should minimally be responsible for:
    - Communication with the doctor agent, to accept the CNP contract, obtain beliefs, and to check critical status of a victim at a given position
    - Determining the next intention of the agent.  This is typically to go to some location based on
      - If a critical victim is found (go to hospital)
      - if a non critical victim is found (go to hospital if no more non-critical victims are left)
      - if a non critical victim should be rescued (if all victim locations are found, and there are no critical victims rescued, then go to neatest victim)
      - if all victims have been rescued, go home
    - Informing the status of a discovered victim
  - The **JASON Environment** should minimally be responsible for:
    - Communicating with the EV3 brick
    - Displaying the map representing the current status of the mission
    - Determining the next closest victim location to be explored, or closest victim to be rescued
  - The **EV3 Reactive agent** should minimally be responsible for:
    - Movement and localisation
    - Avoiding obstacles
    - Reporting the location and colour of a victim
  - Note that either the EV3 agent can do path planning, or the JASON agent can do this.  It is up to you.  Please do not attempt this in AgentSpeak!
    
### Environment

- Each **square arena** will be configured to be 6x6 cells in size, where each cell is 25x25cm.  These cells will be marked out on each of the bays (or arenas) in the Robotics Teaching Lab (Lab 4).
- The arena will have a small number (between 2 and 4) of obstacles, which will be placed at pre-defined locations during the demo (note that the location of these obstacles is known by the doctor agent). These obstacles will be 25x25x10cm in dimension (i.e. the white obstacles used in the Robot Lab). They are intended to be easily detectable, but should not move when collisions occur.  
- The arena will have three coloured squares (**cyan/teal** or **burgundy**), that will be placed in pre-defined locations during the demo.  These squares denote the existence of a victim, and its critical status. 
- During the assignment period:
  - Each bay will have a different configuration of obstacles and victim locations, known by the doctor agent for that Bay.
- During the demos:
  - A new configuration of victims and obstacles will be used.  You will be given the code for the new doctor agent during the demo.
- In one of the corners (denoted by a **yellow square** and an **Infra-Red beacon**), the hospital is located.  This is the location where the paramedic agent should take the victim as part of its mission, and where it should return at the end of the demo.
- One of the corners is currently denoted by a **green square**), but has no meaning in this assignment with respect to victim status.  Treat this as a normal cell, although you can exploit this cell for localisation (e.g. with the particle filter)

### Robot Configuration

- Each group can choose whether to start the robot from a known location, or for up to 8 additional marks, from a random location.
  - **Known Location:**
    - The robot can start from a known location, corresponding to the hospital located in the bottom left of the grid (i.e. at cell 0,0) in the centre of the cell (i.e. at approximately 12.5cm right, 12.5cm up), with an orientation of heading North.
  - **Random Location (additional marks):**
    - The robot may start  in an unknown location and unknown orientation.
- The robot will be configured with **two colour sensors** at the front, which can be used to assist with localisation.  A **Gyroscope is located in the centre** of the robot.
- A **range sensor** is positioned at the front of the robot that can rotate between -90 and +90 degrees.  You can **choose to use either an Infra Red Sensor or an Ultrasound Sensor.** Note that:
  - the Infra Red sensor can detect the beacon at the hospital, but its range is poor (typically no more than approximately 50cm);
  - the Ultrasound cannot detect the beacon at the hospital but has a good range (typically greater than 1m).
- **Note that you may exploit the module notes on the Ultrasound Sensor model, but no additional marks will be awarded for using this model.**

### Sample Beliefs

- This is an example of a set of beliefs, similar to those provided by the **doctor agent:**
  - location(hospital,0,0)
  - location(obstacle,2,2)
  - location(obstacle,3,2)
  - location(obstacle,4,3)
  - location(obstacle,5,0)
  - location(victim,0,2)
  - location(victim,1,5)
  - location(victim,2,3)
  - location(victim,4,5)
  - location(victim,5,1)

### Finally

- You can make use of the code from your Assignment 1 solution.  If you can, avoid reimplementing code!
- Don't try to model desires or commitments using the Advanced Goal-Based Programming discussion in Chapter 8 of Bordini's book on AgentSpeak
- As a guide, if you need to work something out that I haven't discussed in the lectures, then please check with me or Josh as you might be trying to go beyond the remit of the assignment
