filename=$(basename "$1")
if [[ ${#filename} -gt 0 ]]; then
  [[ "$(drive list -t "$filename")" =~ ^([^T]*)(Title[^S]*)Size.* ]]
  numToTitle=${#BASH_REMATCH[1]}
  numToSize=${#BASH_REMATCH[2]}
  nameSize=${#filename}
  remToSize=$((numToSize - nameSize))
  #echo "ntt:$numToTitle; nts:$numToSize; ns:$nameSize; rts:$remToSize"
  
  while read -r line ; do
    if [[ "$line" =~ ^(.{31})$filename[\ ]{$remToSize}[0-9].* ]]; then
      if [[ "$line" =~ ([0-9A-Za-z\_]{20,})\ .* ]]; then
        drive delete -i ${BASH_REMATCH[1]}
        #echo "Discovered perfect match, with id: ${BASH_REMATCH[1]}"
      fi
    fi
  done < <(drive list -t "$filename")
  result=$(drive upload -f $1)
  #echo $result
  [[ "$result" =~ Id:\ ([0-9A-Za-z\_]+).*Md5sum:\ ([0-9A-Za-z]+).* ]]
  id="${BASH_REMATCH[1]}"
  md5="${BASH_REMATCH[2]}"
  #echo "Id: $id"
  #echo "MD5: $md5"
  [[ "$filename" =~ .*(v[0-9\.]+)\..* ]]
  echo "${BASH_REMATCH[1]} | [link]() | [link]() | $md5">>versionlinksLine.txt
fi
