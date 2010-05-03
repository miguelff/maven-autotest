package org.apache.maven.contrib;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.regex.Pattern;

public class FileChangeDetector extends Observable {

	/**
	 * Regexp patterns for accepted files. A file must match at least one of
	 * this patterns to be tracked for changes
	 */
	private Set<Pattern> acceptedFiles;

	/**
	 * Regexp patterns for rejected files. A file mustn't match any of this
	 * patterns to be tracked for changes
	 */
	private Set<Pattern> rejectedFiles;

	/**
	 * This is de parent directory to recursively look for files
	 */
	private File observableDirectory;

	/**
	 * Mantains a registry of the files tracked, to know if they have changed
	 */
	private FileTracker fileTracker;

	/**
	 * Initializes a new instance of the file change detector.
	 */ 
	public FileChangeDetector(Set<Pattern> acceptedFiles,
			Set<Pattern> rejectedFiles, File observableDirectory) {
		if (observableDirectory == null || !observableDirectory.isDirectory()) {
			throw new IllegalArgumentException(observableDirectory
					+ " is not a directory");
		}
		fileTracker = new FileTracker();
		this.acceptedFiles = acceptedFiles;
		this.rejectedFiles = rejectedFiles;
		this.observableDirectory = observableDirectory;
	}

	/**
	 * Starts tracking changes.
	 */
	public void start() {
		fileTracker.trackChanges();
	}

	/**
	 * Checks if there were any changes, and if they were, it notifies those changed files to its observers.
	 * Passing a map containing two keys:
	 * <code>files</code> a set of files that were changed since last check
	 * <code>observableDirectory</code> the directory that is being tracked
	 */
	public void checkForChanges() {
		Set<File> filesChanged = fileTracker.trackChanges();
		if (!filesChanged.isEmpty()) {
			HashMap<String, Object> args = new HashMap<String, Object>();
			args.put("files", filesChanged);
			args.put("observableDirectory", observableDirectory);
			setChanged();
			notifyObservers(args);
		}
	}

	/**
	 * Mantains a registry of the files tracked, to know if they have changed
	 * 
	 * @author miguelff
	 * 
	 */
	private class FileTracker {

		Set<FileSnapshot> previousFileList;

		public FileTracker() {
			previousFileList = new HashSet<FileSnapshot>();
		}

		/**
		 * Checks for changes in the tracked files returning a set containing
		 * those files that have changed since last check.
		 */
		private Set<File> trackChanges() {
			Set<FileSnapshot> currentFileList = new HashSet<FileSnapshot>();
			listFiles(observableDirectory, currentFileList, new HashSet<File>());
			Set<FileSnapshot> difference = new HashSet<FileSnapshot>(Arrays
					.asList(currentFileList.toArray(new FileSnapshot[] {})));
			difference.removeAll(previousFileList);
			previousFileList = currentFileList;

			Set<File> differentFiles = new HashSet<File>();
			for (FileSnapshot snapshot : difference) {
				differentFiles.add(snapshot.getFile());
			}
			return differentFiles;
		}

		private void listFiles(File directory, Set<FileSnapshot> listedFiles, Set<File> alreadyVisited) {
			File[] filesInDirectory = directory.listFiles();
			for (File file : filesInDirectory) {
				System.out.println("Exploring file" + file);
				if (file.isDirectory() && file.canRead()
						&& !alreadyVisited.contains(file)) {
					alreadyVisited.add(file);
					listFiles(file, listedFiles, alreadyVisited);
				} else if (isAnAcceptedFile(file)) {
					listedFiles.add(FileSnapshot.createFromFile(file));
				}
			}
		}

		private boolean isAnAcceptedFile(File file) {
			return matchesAnyOfThePatterns(file, acceptedFiles)
					&& !matchesAnyOfThePatterns(file, rejectedFiles);
		}

		private boolean matchesAnyOfThePatterns(File file, Set<Pattern> patterns) {
			for (Iterator<Pattern> it = patterns.iterator(); it.hasNext();) {
				Pattern p = it.next();
				if (p.matcher(file.getName()).matches()) {
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * 
	 * @author miguelff
	 * 
	 * Encapsulates a file, together with when it was last modified. 
	 * 
	 */
	private static class FileSnapshot {

		private File file;
		private String fileName;
		private long lastModified;

		private FileSnapshot(File f) {
			file = f;
			fileName = f.getName();
			lastModified = f.lastModified();
		}

		public static FileSnapshot createFromFile(File file) {
			return new FileSnapshot(file);
		}

		public File getFile() {
			return file;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fileName == null) ? 0 : fileName.hashCode());
			result = prime * result
					+ (int) (lastModified ^ (lastModified >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FileSnapshot other = (FileSnapshot) obj;
			if (fileName == null) {
				if (other.fileName != null)
					return false;
			} else if (!fileName.equals(other.fileName))
				return false;
			if (lastModified != other.lastModified)
				return false;
			return true;
		}

	}

}
