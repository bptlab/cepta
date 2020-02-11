package utils

import (
)

func Contains(s []string, e string) bool {
	for _, a := range s {
		 if a == e {
			 return true
		 }
	 }
	 return false
}

func Unique(slice []int) []int {
    keys := make(map[int]bool)
    list := []int{} 
    for _, entry := range slice {
        if _, value := keys[entry]; !value {
            keys[entry] = true
            list = append(list, entry)
        }
    }    
    return list
}