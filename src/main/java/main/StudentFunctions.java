package main;

import hashdb.HashFile;
import hashdb.HashHeader;
import hashdb.Vehicle;
import misc.ReturnCodes;
import hashdb.MutableInteger;

import java.io.*;

public class StudentFunctions
{
    /**
     * hashCreate
     * This funcAon creates a hash file containing only the HashHeader record.
     * • If the file already exists, return RC_FILE_EXISTS
     * • Create the binary file by opening it.
     * • Write the HashHeader record to the file at RBN 0.
     * • close the file.
     * • return RC_OK.
     */
    public static int hashCreate(String fileName, HashHeader hashHeader)
    {
        //insert the file in a temp var to use it with checker.
        File newFile = new File(fileName);
        int Exists = ReturnCodes.RC_FILE_EXISTS;
        //if it does exist
        //if( ! newfile.exists())
        //{
        //    return ReturnCodes.RC_OK;
        //}
        if(newFile.exists())
        {
            //return code RC
            return Exists;
        }
        try
        {
            //set the raf to a temp var again
            RandomAccessFile ran = new RandomAccessFile(newFile, "rws");
            //setting the hashfile
            HashFile hashFile = new HashFile();
            hashFile.setFile(ran);
            hashFile.setHashHeader(hashHeader);
            //seek tothe posotion of the hasheader to write to it
            hashFile.getFile().seek(0);
            //we write to it
            hashFile.getFile().write(hashFile.getHashHeader().toByteArray());
            hashFile.getFile().close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        return ReturnCodes.RC_OK;
    }

    /**
     * hashOpen
     * This function opens an existing hash file which must contain a HashHeader record
     * , and sets the file member of hashFile
     * It returns the HashHeader record by setting the HashHeader member in hashFile
     * If it doesn't exist, return RC_FILE_NOT_FOUND.
     * Read the HashHeader record from file and return it through the parameter.
     * If the read fails, return RC_HEADER_NOT_FOUND.
     * return RC_OK
     */
    public static int hashOpen(String fileName, HashFile hashFile)
    {
        //check if there is a existing hash file if one does exist we simply return RC.Code
        //set the file to a var here
        File checkopenedFile = new File(fileName);
        int notFound2 = ReturnCodes.RC_FILE_NOT_FOUND;
        if(!checkopenedFile.exists())
        {
            //return ReturnCcodes.Rc)F
            //return code here if it does not exist.
            return notFound2;
        }
        try
            {
                FileInputStream newFile = new FileInputStream((fileName));
                //set the length of the file so we can use it to set the new bytes.
                int len = newFile.available();
                hashFile.setFile( new RandomAccessFile(checkopenedFile, "rws") );
                //set the byte length using the new opened file above
                byte[] bytes = new byte[(int) len];
                //seek to the position
                hashFile.getFile().seek(0);
                //read the file and finish up in
                hashFile.getFile().read(bytes);
                hashFile.getHashHeader().fromByteArray(bytes);
            }
            catch (IOException e)
            {
                //e.printStackTrace();
                System.out.println(e);
            }
        //Ok
        return ReturnCodes.RC_OK;
        }

    /**
     * vehicleInsert
     * This function inserts a vehicle into the specified file.
     * Determine the RBN using the Main class' hash function.
     * Use readRec to read the record at that RBN.
     * If that location doesn't exist
     * OR the record at that location has a blank vehicleId (i.e., empty string):
     * THEN Write this new vehicle record at that location using writeRec.
     * If that record exists and that vehicle's szVehicleId matches, return RC_REC_EXISTS.
     * (Do not update it.)
     * Otherwise, return RC_SYNONYM. a SYNONYM is the same thing as a HASH COLLISION
     * Note that in program #2, we will actually insert synonyms.
     */
    public static int vehicleInsert(HashFile hashFile, Vehicle vehicle)
    {
        int nuim = 0; //rbnPosition = P2Main.hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash());
        Vehicle tmp = vehicle;
        int num = readRec(hashFile,nuim,tmp);
        if ( num == ReturnCodes.RC_LOC_NOT_FOUND || tmp.getVehicleId()[0] == '\0') {
            writeRec(hashFile,nuim,vehicle);
        } else if ( num == ReturnCodes.RC_OK || tmp.getVehicleId().equals(vehicle.getVehicleId()) ) {
            return ReturnCodes.RC_REC_EXISTS;
        } else {
            //collision
            return ReturnCodes.RC_SYNONYM;
        }
        //System.out.println("***/vehicleInsert - leaving.\n");
        return ReturnCodes.RC_OK;
        /*
        int hashheader = hashFile.getHashHeader().getMaxHash();
        int hasher = P2Main.hash(vehicle.getVehicleId(),hashheader);
        int Ok = ReturnCodes.RC_OK;
        int Exists = ReturnCodes.RC_REC_EXISTS;
        int locnotFound2 = ReturnCodes.RC_LOC_NOT_FOUND;

        Vehicle pastV = new Vehicle();
        if (readRec(hashFile,hasher,pastV) == locnotFound2 || pastV.getVehicleIdAsString() == (""))
        {
            writeRec(hashFile,hasher,vehicle);
            return Ok;
        }
            else if(pastV.getVehicleIdAsString() == (vehicle.getVehicleIdAsString()))
             {
                return Exists;
             }
            else
            {
                //if the location has a vehicle inserted already
                return ReturnCodes.RC_SYNONYM;
            }*/
    }

    /**
     * readRec(
     * This function reads a record at the specified RBN in the specified file.
     * Determine the RBA based on RBN and the HashHeader's recSize
     * Use seek to position the file in that location.
     * Read that record and return it through the vehicle parameter.
     * If the location is not found, return RC_LOC_NOT_FOUND.  Otherwise, return RC_OK.
     * Note: if the location is found, that does NOT imply that a vehicle
     * was written to that location.  Why?
      */
    public static int readRec(HashFile hashFile, int rbn, Vehicle vehicle)
    {
        int noClue = rbn*hashFile.getHashHeader().getRecSize();
        RandomAccessFile file1 = hashFile.getFile();
        //declare to codes for it to look nicer
         int notFound = ReturnCodes.RC_FILE_NOT_FOUND;
         int locnotFound = ReturnCodes.RC_LOC_NOT_FOUND;
        //try catch functions
        try {
            //check the rbn to see if it is found or not
            if (noClue > file1.length())
            {
                return locnotFound;
            }
            byte[] bytes = new byte[vehicle.sizeOf()];
            //once again we seek and read
            file1.seek(noClue);
            file1.read(bytes);
            vehicle.fromByteArray(bytes);
            }
        //print
        catch(IOException e)
        {
            System.out.println(e);
            return notFound;
        }
        return notFound;
    }

    /**
     * writeRec
     * This function writes a record to the specified RBN in the specified file.
     * Determine the RBA based on RBN and the HashHeader's recSize
     * Use seek to position the file in that location.
     * Write that record to the file.
     * If the write fails, return RC_LOC_NOT_WRITTEN.
     * Otherwise, return RC_OK.
     */
    public static int writeRec(HashFile hashFile, int rbn, Vehicle vehicle) {
        int rba = rbn * hashFile.getHashHeader().getRecSize();
        int notWritten = ReturnCodes.RC_LOC_NOT_WRITTEN;
        //grab the file data
        RandomAccessFile dooma= hashFile.getFile();
        //seek to the location of the file.getfile
        try{
            dooma.seek(rba);
            // just write to the location we seeked to.
            dooma.write(vehicle.toByteArray());
        }
        catch ( IOException e )
        {
            //print it out
            System.out.println(e);
            //reutnr Rc code
            return notWritten;
        }
        //return RC code
        return notWritten;
    }

    /**
     * vehicleRead
     * This function reads the specified vehicle by its vehicleId.
     * Since the vehicleId was provided,
     * determine the RBN using the Main class' hash function.
     * Use readRec to read the record at that RBN.
     * If the vehicle at that location matches the specified vehicleId,
     * return the vehicle via the parameter and return RC_OK.
     * Otherwise, return RC_REC_NOT_FOUND
     */
    public static int vehicleRead(HashFile hashFile, MutableInteger rbn, Vehicle vehicle) {

        Vehicle currVeh = vehicle;
        int num = readRec(hashFile, rbn, vehicle);
        Vehicle newVeh = vehicle;
        if ( num == ReturnCodes.RC_OK ) {
            if ( currVeh.getVehicleId().equals(newVeh.getVehicleId())) {
                vehicle = newVeh;
            } else {    //is a synonym

            }
        } else {
            System.out.println("****ERROR: Record not found\n");
            return ReturnCodes.RC_REC_NOT_FOUND;
        }
        //System.out.println("****/vehicleRead - leaving.\n");
        return ReturnCodes.RC_OK;

        /*rbn = P2Main.hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash());
        String oldString = vehicle.getVehicleIdAsString();
        int rc = readRec(hashFile,rbn,vehicle);
        if( rc != ReturnCodes.RC_OK || (! vehicle.getVehicleIdAsString().equals(oldString)) )
            {
                return ReturnCodes.RC_REC_NOT_FOUND;
            }
        return ReturnCodes.RC_REC_NOT_FOUND;*/
    }

    public static int vehicleUpdate(HashFile hashFile, Vehicle vehicle) {
        int rbn =  P2Main.hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash());
        int num = readRec(hashFile, rbn, vehicle);
        if ( num == ReturnCodes.RC_OK) {
            //old and new both have the same id so we use that to find rbn and rba in writeRec. Delete it and just rewrite it at the same block
            vehicleDelete(hashFile,vehicle.getVehicleId()); //this should work
            writeRec(hashFile,rbn,vehicle);
        } else {
            return ReturnCodes.RC_REC_NOT_FOUND;
        }
        /*Note that this function must understand probing.
        NOTE: You can make your life easier with this function if you use MutableInteger and call some of your other functions to help out
        */
        return ReturnCodes.RC_OK;
    }

    //EXTRA CREDIT FUNCTION!!!
    public static int vehicleDelete(HashFile hashFile, char[] vehicleId) {
        try {
            byte[] b = {'\0'};
            int rbn = P2Main.hash(vehicleId,hashFile.getHashHeader().getMaxHash());
            long rba = (long) rbn * hashFile.getHashHeader().getRecSize();
            RandomAccessFile toRemove = new RandomAccessFile("vehicle.dat","rw");
            toRemove.seek(rba);
            toRemove.write(b,0,hashFile.getHashHeader().getRecSize());
            toRemove.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReturnCodes.RC_OK;
    }
}



