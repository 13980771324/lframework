local n = 1;
print("in ba ok ",n);

n = n + 1;

return {
	
	test = function()
		return "tm ".. os.time() .. "|" .. n;
	end
}