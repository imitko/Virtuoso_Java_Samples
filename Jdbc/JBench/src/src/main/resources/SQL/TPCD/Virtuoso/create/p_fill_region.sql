--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure tpcd..fill_region(in n integer) {
	
	declare _r_regionkey integer;
	declare _r_name, _r_comment varchar;
	
	declare namearray, regionarray integer;
	
	namearray := vector ('AFRICA', 'AMERICA', 'ASIA', 'EUROPE', 'MIDDLE EAST');
				 
	_r_regionkey := 0;
	while (_r_regionkey <= 4) {
		
		_r_name := aref(namearray, _r_regionkey);
		_r_comment := tpcd..randomString(95, 0, 1);
					
		insert into tpcd..region (r_regionkey, r_name, r_comment) values (_r_regionkey, _r_name, _r_comment);
		
		_r_regionkey := _r_regionkey + 1;
	}
}
